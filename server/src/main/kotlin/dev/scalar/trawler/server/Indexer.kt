package dev.scalar.trawler.server

import dev.scalar.trawler.server.db.Entity
import dev.scalar.trawler.server.db.FacetLog
import dev.scalar.trawler.server.db.FacetType
import dev.scalar.trawler.server.db.FacetValue
import dev.scalar.trawler.server.db.util.selectForUpdate
import dev.scalar.trawler.server.schema.TypeRegistry
import dev.scalar.trawler.server.schema.TypeRegistryImpl
import io.vertx.core.eventbus.Message
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.receiveChannelHandler
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant
import java.util.*

class Indexer(val typeRegistry: TypeRegistry = TypeRegistryImpl()) : CoroutineVerticle() {
    val log = LogManager.getLogger()

    private suspend fun indexEntities(projectId: UUID, urns: List<String>): Map<String, UUID> {
        Entity.batchInsert(urns, ignore=true) {
            this[Entity.projectId] = projectId
            this[Entity.createdAt] = Instant.now()
            this[Entity.urn] = it
        }

        return Entity.select { Entity.urn.inList(urns) }
            .associate { it[Entity.urn] to it[Entity.id].value}
    }

    override suspend fun start() {
        val adapter = vertx.receiveChannelHandler<Message<String>>()
        var counter = 0
        vertx.eventBus().localConsumer<String>("indexer.queue").handler(adapter)

        for (message in adapter) {
            val id = UUID.fromString(message.body())
            counter += 1

            if (counter % 100 == 0) {
                log.info("indexing: ${counter}")
            }
            newSuspendedTransaction {
                // Grab the log
                val facetLog = FacetLog
                    .selectForUpdate { FacetLog.id.eq(id) }
                    .first()

                val facetType = typeRegistry.facetTypeById(facetLog[FacetLog.typeId].value)!!

                // Create any entities
                val urns = if (facetType.metaType == FacetType.MetaType.RELATIONSHIP) {
                    listOf(facetLog[FacetLog.entityUrn]) + facetLog[FacetLog.value]!!.map { it.toString() }
                } else {
                    listOf(facetLog[FacetLog.entityUrn])
                }

                val entities = indexEntities(facetLog[FacetLog.projectId].value, urns)
                val entityId = entities[facetLog[FacetLog.entityUrn]]!!

                // Lookup the table
                val facetValues = FacetValue.select {
                    FacetValue.entityId.eq(entityId)
                        .and(FacetValue.typeId.eq(facetLog[FacetLog.typeId].value))
                }.toList()

                if (facetValues.any { it[FacetValue.version] > facetLog[FacetLog.version] }) {
                   log.info("Stale write detected: ${id}")
                } else {
                    // Delete any old versions
                    FacetValue.deleteWhere {
                        FacetValue.entityId.eq(entityId)
                            .and(FacetValue.version.less(facetLog[FacetLog.version]))
                            .and(FacetValue.typeId.eq(facetLog[FacetLog.typeId].value))
                    }

                    val indexedValues = facetLog[FacetLog.value]!!.withIndex()

                    // Add new versions
                    FacetValue.batchInsert(indexedValues) {
                        this[FacetValue.projectId] = facetLog[FacetLog.projectId].value
                        this[FacetValue.entityId] = entityId
                        this[FacetValue.typeId] = facetType.id
                        this[FacetValue.index] = it.index.toShort()
                        this[FacetValue.version] = facetLog[FacetLog.version]
                        this[FacetValue.updatedAt] = Instant.now()

                        if (facetType.metaType == FacetType.MetaType.RELATIONSHIP) {
                            this[FacetValue.targetEntityId] = entities[it.value.toString()]
                        } else if (facetType.metaType == FacetType.MetaType.TYPE_REFERENCE) {
                            this[FacetValue.entityTypeId] = UUID.fromString(it.value as String)
                        } else {
                            this[FacetValue.value] = it.value as Any
                        }
                    }
                }

                // Update the log
                FacetLog.update({ FacetLog.id.eq(id) }) {
                    it[FacetLog.entityId] = entities[facetLog[FacetLog.entityUrn]]
                }
            }
        }
    }
}