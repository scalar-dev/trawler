package dev.scalar.trawler.server.graphql

import dev.scalar.trawler.server.db.EntityType
import dev.scalar.trawler.server.db.FacetLog
import dev.scalar.trawler.server.db.FacetType
import dev.scalar.trawler.server.db.customDistinctOn
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class EntityQuery {
    private suspend fun fetchEntities(ids: List<UUID>): List<dev.scalar.trawler.server.graphql.Entity> {
        val facets = fetchFacets(ids)

        val types = transaction {
            dev.scalar.trawler.server.db.Entity
                .join(dev.scalar.trawler.server.db.EntityType, JoinType.LEFT, dev.scalar.trawler.server.db.EntityType.id, dev.scalar.trawler.server.db.Entity.typeId)
                .select { dev.scalar.trawler.server.db.Entity.id.inList(ids) }
                .associate { it[ dev.scalar.trawler.server.db.Entity.id].value to it[EntityType.uri] }
        }

        return ids.map { entityId ->
            val facetsByUri = facets
                .filter { it.entityId == entityId }
                .groupBy { it.uri }
                .flatMap {
                    val version = it.value.maxOf { facet -> facet.version }
                    it.value.filter { facet -> facet.version == version }
                }

            Entity(
                entityId,
                types[entityId]!!,
                facetsByUri
            )
        }
    }

    private suspend fun fetchFacets(ids: List<UUID>) = newSuspendedTransaction {
        FacetLog
            .join(FacetType, JoinType.LEFT, FacetType.id, FacetLog.typeId)
            .slice(customDistinctOn(FacetLog.entityId, FacetLog.typeId, FacetLog.index), FacetLog.entityId, FacetType.uri, FacetType.metaType, FacetLog.value, FacetLog.version, FacetLog.targetEntityId)
            .select { FacetLog.entityId.inList(ids) }
            .orderBy( FacetLog.entityId to SortOrder.DESC, FacetLog.typeId to SortOrder.DESC, FacetLog.index to SortOrder.DESC, FacetLog.version to SortOrder.DESC)
            .map { Facet(it[FacetLog.entityId].value, it[FacetType.uri], it[FacetLog.version], it[FacetLog.value] ?: it[FacetLog.targetEntityId].value.toString(), it[FacetType.metaType]) }
    }

    private suspend fun fetchFacetsTo(ids: List<UUID>) = newSuspendedTransaction {
        val max = FacetLog.version.max().alias("max_version")

        val maxVersion = FacetLog
            .slice(FacetLog.entityId, FacetLog.typeId, max)
            .selectAll()
            .groupBy(FacetLog.entityId, FacetLog.typeId)
            .alias("max_version")

        dev.scalar.trawler.server.db.FacetLog
            .join(FacetType, JoinType.INNER, FacetType.id, FacetLog.typeId)
            .join(maxVersion, JoinType.INNER, maxVersion[FacetLog.entityId], FacetLog.entityId) {
                maxVersion[FacetLog.typeId].eq(
                    FacetLog.typeId
                ) and maxVersion[max].eq(FacetLog.version)
            }
            .select { FacetLog.targetEntityId.inList(ids) }
            .map { it[FacetLog.entityId].value }
    }

    suspend fun entityById(id: UUID): List<dev.scalar.trawler.server.graphql.Entity> {
        val entity = fetchEntities(listOf(id)).first()

        val targetIds = entity.facets.filter {
            it.metaType == FacetType.MetaType.RELATIONSHIP.value ||
                    it.metaType == FacetType.MetaType.RELATIONSHIP_OWNED.value
        }.map { UUID.fromString(it.value as String) }

        val fromIds = fetchFacetsTo(listOf(id))
        val linkedEntities = fetchEntities(targetIds + fromIds)

        return listOf(entity) + linkedEntities
    }
}