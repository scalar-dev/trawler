package dev.scalar.trawler.server.collect

import dev.scalar.trawler.server.FacetSnapshot
import dev.scalar.trawler.server.FacetSnapshotValue
import dev.scalar.trawler.server.db.Entity
import dev.scalar.trawler.server.db.FacetLog
import dev.scalar.trawler.server.db.FacetType
import dev.scalar.trawler.server.schema.TypeRegistryImpl
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*
import kotlin.system.measureTimeMillis

class FacetStore {
    val typeRegistry = TypeRegistryImpl()
    val log = LogManager.getLogger()

    suspend fun ingestFacet(projectId: UUID, snapshot: FacetSnapshot, urnsToEntityIds: Map<String, UUID>) {
        newSuspendedTransaction {
            val version = FacetLog
                .slice(FacetLog.version)
                .select { FacetLog.entityId.eq(urnsToEntityIds[snapshot.entityUrn]).and(FacetLog.typeId.eq(snapshot.facetType.id)) }
                .orderBy(FacetLog.version, SortOrder.DESC)
                .limit(1)
                .firstOrNull()?.get(FacetLog.version) ?: 0

            val idSnapshotValues = snapshot.values.filterIsInstance<FacetSnapshotValue.Id>()

            val entityIds = Entity
                .slice(Entity.id, Entity.urn)
                .select { Entity.urn inList idSnapshotValues.map { it.value } }
                .associate { it[Entity.urn] to it[Entity.id].value }

            val indexedValues = snapshot.values.mapIndexed { index, facetSnapshotValue ->  index to facetSnapshotValue }

            // Need to check types here
            FacetLog.batchInsert(indexedValues) { indexValue ->
                val index = indexValue.first
                val value = indexValue.second

                this[FacetLog.projectId] = projectId
                this[FacetLog.typeId] = snapshot.facetType.id
                this[FacetLog.version] = version + 1
                this[FacetLog.index] = index.toShort()
                this[FacetLog.entityId] = urnsToEntityIds[snapshot.entityUrn]

                when (value) {
                    is FacetSnapshotValue.Id -> {
                        assert(
                            snapshot.facetType.metaType == FacetType.MetaType.RELATIONSHIP ||
                                    snapshot.facetType.metaType == FacetType.MetaType.RELATIONSHIP_OWNED)
                        this[FacetLog.targetEntityId] = entityIds[value.value]
                    }
                    is FacetSnapshotValue.String -> {
                        this[FacetLog.value] = value.value
                    }
                    else -> {
                        this[FacetLog.value] = value
                    }
                }
            }
            version
        }
    }

    suspend fun ingest(projectId: UUID, request: CollectRequest) {
        val time = measureTimeMillis {
            // Collect all facets
            val facetSnapshots = request.nodes
                .flatMap { node ->
                    node.facets().map { keyValue ->
                        val facetType = typeRegistry.facetTypeByUri(keyValue.key)

                        if (facetType == null) {
                            log.warn("Unrecognised facet ${keyValue.key}. Skipping")
                            null
                        } else {
                            FacetSnapshot(
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
                .filterNotNull()

            // Find all referenced URNs
            val nodeUrns = request.nodes.map { node -> node.id }
            val ghostUrns = facetSnapshots.flatMap { snapshot ->
                snapshot.values.filterIsInstance<FacetSnapshotValue.Id>()
                    .map { value -> value.value }
            }

            // Look up all relevant types
            val entityTypes = (nodeUrns + ghostUrns)
                .toSet()
                .associateWith { urn -> typeRegistry.entityTypeByUrn(urn) }
                .filterValues { it != null }

            // Upsert all URNs
            log.info("ingesting ${entityTypes.keys.size} entities")
            val entityIds = newSuspendedTransaction {
                Entity.batchInsert(entityTypes.keys, ignore = true, shouldReturnGeneratedValues = true) { urn ->
                    this[Entity.projectId] = projectId
                    this[Entity.urn] = urn
                    this[Entity.typeId] = entityTypes[urn]!!.id
                }

                Entity
                    .slice(Entity.urn, Entity.id)
                    .select { Entity.urn inList nodeUrns + ghostUrns }
                    .associate { it[Entity.urn] to it[Entity.id].value }
            }

            val validFacets = facetSnapshots.filter { facetSnapshot ->  entityTypes[facetSnapshot.entityUrn] != null }

            log.info("ingesting ${validFacets.size} facets")
            validFacets.forEach { facetSnapshot -> ingestFacet(projectId, facetSnapshot, entityIds) }
        }

        log.info("took ${time}ms")
    }
}