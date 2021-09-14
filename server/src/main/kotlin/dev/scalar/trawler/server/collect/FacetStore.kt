package dev.scalar.trawler.server.collect

import dev.scalar.trawler.server.db.FacetLog
import dev.scalar.trawler.server.db.FacetType
import dev.scalar.trawler.server.schema.TypeRegistry
import dev.scalar.trawler.server.schema.TypeRegistryImpl
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

class FacetStore {
    val typeRegistry = TypeRegistryImpl()
    val log = LogManager.getLogger()

    data class StoreReult(
        val txId: UUID,
        val ids: List<UUID>,
        val unrecognisedFacetTypes: Set<String>,
        val unrecognisedEntityTypes: Set<String>
    )

    suspend fun ingestFacet(snapshot: FacetSnapshot): UUID = newSuspendedTransaction {
            val version = FacetLog
                .slice(FacetLog.version)
                .select { FacetLog.entityUrn.eq(snapshot.entityUrn).and(FacetLog.typeId.eq(snapshot.facetType.id)) }
                .orderBy(FacetLog.version, SortOrder.DESC)
                .limit(1)
                .firstOrNull()?.get(FacetLog.version) ?: 0

            // TODO: check types
            // TODO: resolve URNs
            FacetLog.insertAndGetId {
                it[FacetLog.projectId] = snapshot.projectId
                it[FacetLog.txId] = snapshot.txId
                it[FacetLog.typeId] = snapshot.facetType.id
                it[FacetLog.version] = version + 1
                it[FacetLog.entityUrn] = snapshot.entityUrn
                it[FacetLog.value] = snapshot.values.map { value ->
                    when (value) {
                        is FacetSnapshotValue.Id -> {
                            assert(
                                snapshot.facetType.metaType == FacetType.MetaType.RELATIONSHIP
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
            }.value
        }

    suspend fun ingest(projectId: UUID, request: CollectRequest): StoreReult {
        val unrecognisedFacetTypes = mutableSetOf<String>()
        val unrecognisedEntityTypes = mutableSetOf<String>()

        val txId = UUID.randomUUID()

        // Collect all facets
        val ingestOps = request.nodes
            .flatMap { node ->
                val entityType = typeRegistry.entityTypeByUri(node.type[0])

                if (entityType == null) {
                    unrecognisedEntityTypes.add(node.type[0])
                    emptyList()
                } else {
                    listOf(
                        FacetSnapshot(
                            projectId,
                            txId,
                            node.id,
                            typeRegistry.facetTypeByUri(TypeRegistry.ENTITY_TYPE)!!,
                            node.type.map { FacetSnapshotValue.String(entityType.id.toString()) }
                        )
                    ) + node.facets().map { keyValue ->
                        val facetType = typeRegistry.facetTypeByUri(keyValue.key)

                        if (facetType == null) {
                            unrecognisedFacetTypes.add(keyValue.key)
                            null
                        } else {
                            FacetSnapshot(
                                projectId,
                                txId,
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
        return StoreReult(
            txId,
            ingestOps.map { ingestOp ->
                ingestFacet(ingestOp)
            },
            unrecognisedFacetTypes,
            unrecognisedEntityTypes
        )
    }
}