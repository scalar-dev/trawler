package dev.scalar.trawler.server.graphql.query

import dev.scalar.trawler.ontology.FacetMetaType
import dev.scalar.trawler.ontology.Ontology
import dev.scalar.trawler.server.db.AccountRole
import dev.scalar.trawler.server.db.FacetValue
import dev.scalar.trawler.server.db.Project
import dev.scalar.trawler.server.db.util.ilike
import dev.scalar.trawler.server.graphql.Entity
import dev.scalar.trawler.server.graphql.QueryContext
import dev.scalar.trawler.server.graphql.fetchEntities
import dev.scalar.trawler.server.ontology.OntologyCache
import org.jetbrains.exposed.sql.Alias
import org.jetbrains.exposed.sql.ColumnSet
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.TextColumnType
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.castTo
import org.jetbrains.exposed.sql.compoundOr
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class EntityQuery {
    data class Filter(
        val uri: String,
        val value: List<String>
    )

    private fun SqlExpressionBuilder.filterToOp(columnSet: Alias<FacetValue>, ontology: Ontology, filter: Filter): Op<Boolean> {
        val facetType = ontology.facetTypeByUri(filter.uri)!!

        return columnSet[FacetValue.typeId].eq(facetType.id) and when (facetType.metaType) {
            FacetMetaType.STRING -> {
                filter.value.map { value ->
                    columnSet[FacetValue.value].castTo<String>(TextColumnType())
                        .ilike("%$value%")
                }.compoundOr()
            }
            FacetMetaType.TYPE_REFERENCE -> {
                val entityTypes = filter.value
                    .mapNotNull { value -> ontology.entityTypeByUri(value)?.id }
                columnSet[FacetValue.entityTypeId].inList(entityTypes)
            }
            else -> {
                throw NotImplementedError()
            }
        }
    }

    suspend fun search(context: QueryContext, project: String, filters: List<Filter>): List<Entity> {
        val projectId = newSuspendedTransaction {
            Project
                .innerJoin(AccountRole)
                .slice(Project.id)
                .select { Project.slug.eq(project) and AccountRole.accountId.eq(context.accountId) }
                .firstOrNull()?.get(Project.id)?.value
        } ?: throw Exception("Project not found: $project")

        val ontology = OntologyCache.CACHE[projectId]

        val ids = transaction {

            val aliases = filters.mapIndexed { index, filter ->
                filter.uri to FacetValue.alias("filter_$index")
            }.associate { it.first to it.second }

            val firstAlias = aliases[filters[0].uri]!!
            var query: ColumnSet = firstAlias

            filters.drop(1).forEachIndexed { index, filter ->
                val alias = aliases[filter.uri]!!
                query = query.innerJoin(alias, { firstAlias[FacetValue.entityId] }, { alias[FacetValue.entityId] })
            }

            query
                .slice(firstAlias[FacetValue.entityId])
                .select {
                    val firstFilter = filters[0]
                    val initialOp = filterToOp(aliases[firstFilter.uri]!!, ontology, firstFilter)

                    filters.drop(1).fold(initialOp) { op, filter ->
                        op.and(
                            filterToOp(
                                aliases[filter.uri]!!,
                                ontology,
                                filter
                            )
                        )
                    }
                }
                .map { row -> row[firstAlias[FacetValue.entityId]] }
        }

        return fetchEntities(context.accountId, ids)
            .filter { it.projectId == projectId }
    }

    suspend fun entity(context: QueryContext, id: UUID) = fetchEntities(context.accountId, listOf(id)).firstOrNull()

    suspend fun entityGraph(context: QueryContext, id: UUID, d: Int): List<Entity> {
        var currentEntities = fetchEntities(context.accountId, listOf(id))
        val output = currentEntities.toMutableList()

        for (i in 0 until d) {
            val targetIds = currentEntities.flatMap { entity ->
                entity.facets.filter {
                    it.metaType == FacetMetaType.RELATIONSHIP.value
                }
                    .flatMap { (it.value as List<UUID>) }
            }

            val fromIds = transaction {
                FacetValue
                    .join(dev.scalar.trawler.server.db.Entity, JoinType.INNER, dev.scalar.trawler.server.db.Entity.id, FacetValue.entityId)
                    .slice(dev.scalar.trawler.server.db.Entity.id)
                    .select { FacetValue.targetEntityId.inList(currentEntities.map { it.entityId }) }
                    .map { it[dev.scalar.trawler.server.db.Entity.id].value }
            }

            val existingIds = output.map { it.entityId }.toSet()

            val idsToFetch = (targetIds + fromIds).filter { !existingIds.contains(it) }

            currentEntities = fetchEntities(context.accountId, idsToFetch)
            output.addAll(currentEntities)
        }

        return output
    }
}
