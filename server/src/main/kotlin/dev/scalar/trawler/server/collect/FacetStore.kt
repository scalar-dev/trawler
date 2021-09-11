package dev.scalar.trawler.server.collect

import dev.scalar.trawler.server.db.Entity
import dev.scalar.trawler.server.db.FacetLog
import dev.scalar.trawler.server.db.FacetType
import dev.scalar.trawler.server.db.insertOrUpdate
import dev.scalar.trawler.server.schema.TypeRegistryImpl
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant
import java.util.*
import kotlin.system.measureTimeMillis

class FacetStore {
    val typeRegistry = TypeRegistryImpl()
    val log = LogManager.getLogger()

    suspend fun ingestFacet(snapshot: IngestOp.FacetSnapshot) {
        newSuspendedTransaction {
            val version = FacetLog
                .slice(FacetLog.version)
                .select { FacetLog.entityUrn.eq(snapshot.entityUrn).and(FacetLog.typeId.eq(snapshot.facetType.id)) }
                .orderBy(FacetLog.version, SortOrder.DESC)
                .limit(1)
                .firstOrNull()?.get(FacetLog.version) ?: 0

            // Need to check types here
            FacetLog.insert {
                it[FacetLog.projectId] = snapshot.projectId
                it[FacetLog.typeId] = snapshot.facetType.id
                it[FacetLog.version] = version + 1
                it[FacetLog.entityUrn] = snapshot.entityUrn
                it[FacetLog.value] = snapshot.values.map { value ->
                    when (value) {
                        is FacetSnapshotValue.Id -> {
                            assert(
                                snapshot.facetType.metaType == FacetType.MetaType.RELATIONSHIP ||
                                        snapshot.facetType.metaType == FacetType.MetaType.RELATIONSHIP_OWNED
                            )
                            value.value
                        }
                        is FacetSnapshotValue.String -> {
                            value.value
                        }
                        else -> {
                            value
                        }
                    }
                }
            }
            version
        }
    }

    private suspend fun ingestEntity(snapshot: IngestOp.EntitySnapshot) = newSuspendedTransaction {
       Entity.insertOrUpdate(Entity.urn, insert={
           it[Entity.urn] = snapshot.entityUrn
           it[Entity.typeId] = snapshot.entityType.id
           it[Entity.projectId] = snapshot.projectId
       }) {
           it[Entity.typeId] = snapshot.entityType.id
           it[Entity.updatedAt] = Instant.now()
       }
    }

    suspend fun ingest(projectId: UUID, request: CollectRequest) {
        val time = measureTimeMillis {
            // Collect all facets
            val ingestOps = request.nodes
                .flatMap { node ->
                    val entityType = typeRegistry.entityTypeByUri(node.type[0])

                    if (entityType == null) {
                        emptyList()
                    } else {
                        listOf(
                            IngestOp.EntitySnapshot(
                                projectId,
                                node.id,
                                entityType
                            )
                        ) + node.facets().map { keyValue ->
                            val facetType = typeRegistry.facetTypeByUri(keyValue.key)

                            if (facetType == null) {
                                log.warn("Unrecognised facet ${keyValue.key}. Skipping")
                                null
                            } else {
                                IngestOp.FacetSnapshot(
                                    projectId,
                                    node.id,
                                    facetType,
                                    keyValue.value.map { value ->
                                        when {
                                            value.value != null -> {
                                                FacetSnapshotValue.String(value.value)
                                            }
                                            value.id != null -> {
                                                FacetSnapshotValue.Id(value.id)
                                            }
                                            else -> {
                                                throw IllegalArgumentException("Malformed collect facet: $value")
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                .filterNotNull()

            log.info("ingesting ${ingestOps.size} things")
            ingestOps.forEach { ingestOp ->
                when (ingestOp) {
                    is IngestOp.FacetSnapshot -> ingestFacet(ingestOp)
                    is IngestOp.EntitySnapshot -> ingestEntity(ingestOp)
                }
            }
        }

        log.info("took ${time}ms")
    }
}