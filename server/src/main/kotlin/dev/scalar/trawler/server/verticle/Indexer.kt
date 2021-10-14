package dev.scalar.trawler.server.verticle

import dev.scalar.trawler.ontology.FacetMetaType
import dev.scalar.trawler.server.db.Entity
import dev.scalar.trawler.server.db.FacetLog
import dev.scalar.trawler.server.db.FacetTimeSeries
import dev.scalar.trawler.server.db.FacetValue
import dev.scalar.trawler.server.db.util.selectForUpdate
import dev.scalar.trawler.server.ontology.OntologyCache
import io.vertx.core.eventbus.Message
import io.vertx.kotlin.coroutines.receiveChannelHandler
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.UUID

class Indexer : BaseVerticle() {
    val log = LogManager.getLogger()

    private suspend fun indexEntities(projectId: UUID, urns: List<String>): Map<String, UUID> {
        Entity.batchInsert(urns, ignore = true) {
            this[Entity.projectId] = projectId
            this[Entity.createdAt] = Instant.now()
            this[Entity.urn] = it
        }

        return Entity.select { Entity.urn.inList(urns) }
            .associate { it[Entity.urn] to it[Entity.id].value }
    }

    suspend fun indexFacetLog(id: UUID) = newSuspendedTransaction {
        // Grab the log
        val facetLog = FacetLog
            .selectForUpdate { FacetLog.id.eq(id) }
            .first()

        val facetType = OntologyCache.CACHE[facetLog[FacetLog.projectId].value]
            .facetTypeById(facetLog[FacetLog.typeId].value)!!

        // Create any entities
        val urns = if (facetType.metaType == FacetMetaType.RELATIONSHIP) {
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

        if (facetType.indexTimeSeries) {
            val value = facetLog[FacetLog.value]!![0]

            if (value is Number) {
                FacetTimeSeries.insertIgnore {
                    it[FacetTimeSeries.entityId] = entityId
                    it[FacetTimeSeries.typeId] = facetType.id
                    it[FacetTimeSeries.version] = facetLog[FacetLog.version]
                    it[FacetTimeSeries.timestamp] = facetLog[FacetLog.timestamp] ?: facetLog[FacetLog.createdAt]

                    if (facetType.metaType == FacetMetaType.DOUBLE) {
                        it[FacetTimeSeries.valueDouble] = value.toDouble()
                    } else if (facetType.metaType == FacetMetaType.INT) {
                        it[FacetTimeSeries.valueLong] = value.toLong()
                    }
                }
            } else {
                log.warn("skipping time series index for facet $id since it is not Number")
            }
        }

        if (facetValues.any { it[FacetValue.version] > facetLog[FacetLog.version] }) {
            log.info("Stale write detected: $id")
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

                if (facetType.metaType == FacetMetaType.RELATIONSHIP) {
                    this[FacetValue.targetEntityId] = entities[it.value.toString()]
                } else if (facetType.metaType == FacetMetaType.TYPE_REFERENCE) {
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

    override suspend fun start() {
        super.start()
        configureDatabase()

        val adapter = vertx.receiveChannelHandler<Message<String>>()
        var counter = 0
        vertx.eventBus().localConsumer<String>("indexer.queue").handler(adapter)

        for (message in adapter) {
            val id = UUID.fromString(message.body())
            counter += 1

            if (counter % 100 == 0) {
                log.info("indexing: $counter")
            }

            try {
                indexFacetLog(id)
            } catch (e: Throwable) {
                log.error("Exception while trying to index facet log $id", e)
            }
        }
    }
}
