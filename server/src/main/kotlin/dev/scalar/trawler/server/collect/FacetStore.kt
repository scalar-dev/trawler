package dev.scalar.trawler.server.collect

import dev.scalar.trawler.ontology.FacetMetaType
import dev.scalar.trawler.ontology.Ontology
import dev.scalar.trawler.server.db.FacetLog
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class FacetStore(val ontology: Ontology) {
    val log = LogManager.getLogger()

    data class StoreReult(
        val txId: UUID,
        val ids: List<UUID>,
        val unrecognisedFacetTypes: Set<String>,
        val unrecognisedEntityTypes: Set<String>,
        val invalidFacetTypes: Set<String>
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
                            snapshot.facetType.metaType == FacetMetaType.RELATIONSHIP
                        )
                        value.value
                    }
                    is FacetSnapshotValue.Value -> {
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
        val failedValidation = mutableSetOf<String>()

        val txId = UUID.randomUUID()

        // Collect all facets
        val ingestOps = request.nodes
            .flatMap { node ->
                val entityType = ontology.entityTypeByUri(node.type[0])

                if (entityType == null) {
                    unrecognisedEntityTypes.add(node.type[0])
                    emptyList()
                } else {
                    listOf(
                        FacetSnapshot(
                            projectId,
                            txId,
                            node.id,
                            ontology.facetTypeByUri(Ontology.ENTITY_TYPE)!!,
                            node.type.map { FacetSnapshotValue.Value(entityType.id.toString()) }
                        )
                    ) + node.facets().map { keyValue ->
                        val facetType = ontology.facetTypeByUri(keyValue.key)

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
                                            FacetSnapshotValue.Value(value.value)
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

        val validatedIngestOps = ingestOps.filter { ingestOp ->
            if (!ingestOp.values.all {
                when (it) {
                    is FacetSnapshotValue.Value -> ingestOp.facetType.validate(it.value)
                    else -> true
                }
            }
            ) {
                log.warn("Facet ${ingestOp.facetType.uri} violates JSON schema. Skipping")
                failedValidation.add(ingestOp.facetType.uri)
                false
            } else {
                true
            }
        }

        log.info("ingesting ${ingestOps.size} things")
        return StoreReult(
            txId,
            validatedIngestOps.map { ingestOp ->
                ingestFacet(ingestOp)
            },
            unrecognisedFacetTypes,
            unrecognisedEntityTypes,
            failedValidation
        )
    }
}
