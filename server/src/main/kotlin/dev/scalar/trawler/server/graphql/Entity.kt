package dev.scalar.trawler.server.graphql

import dev.scalar.trawler.ontology.FacetMetaType
import dev.scalar.trawler.server.db.Entity
import dev.scalar.trawler.server.db.FacetLog
import dev.scalar.trawler.server.db.FacetTimeSeries
import dev.scalar.trawler.server.db.FacetType
import dev.scalar.trawler.server.ontology.OntologyCache
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

data class Entity(
    val entityId: UUID,
    val urn: String,
    val type: String,
    val typeName: String,
    val facets: List<Facet>
) {
    suspend fun facetLog(context: QueryContext, facets: List<String>): List<dev.scalar.trawler.server.graphql.FacetLog> = newSuspendedTransaction {
        val rows = FacetLog
            .join(Entity, JoinType.INNER, FacetLog.entityUrn, Entity.urn)
            .join(FacetType, JoinType.INNER, FacetLog.typeId, FacetType.id)
            .select {
                FacetLog.entityId.eq(entityId) and FacetType.uri.inList(facets)
            }
            .orderBy(FacetLog.version, SortOrder.DESC)

        val urns = rows.flatMap { row ->
            val values = row[FacetLog.value]
            values?.map { it.toString() } ?: emptyList<String>()
        }

        val entities = Entity
            .select { Entity.urn.inList(urns) }
            .map { row -> row[Entity.id].value }

        val entitiesByUrn = fetchEntities(entities)
            .associateBy { it.urn }

        rows.map { row ->
            val facetType = OntologyCache.CACHE[context.projectId].facetTypeById(row[FacetLog.typeId].value)!!

            dev.scalar.trawler.server.graphql.FacetLog(
                row[FacetLog.id].value,
                facetType.name,
                facetType.uri,
                row[FacetLog.version],
                row[FacetLog.value]?.mapNotNull { entitiesByUrn[it.toString()] } ?: emptyList(),
                row[FacetLog.createdAt]
            )
        }
    }

    suspend fun timeSeries(context: QueryContext, facet: String): dev.scalar.trawler.server.graphql.FacetTimeSeries? = newSuspendedTransaction {
        val facetType = OntologyCache.CACHE[context.projectId].facetTypeByUri(facet)!!

        val rows = FacetTimeSeries
            .join(FacetType, JoinType.INNER, FacetTimeSeries.typeId, FacetType.id)
            .select {
                FacetTimeSeries.entityId.eq(entityId) and FacetType.uri.eq(facet)
            }
            .orderBy(FacetTimeSeries.timestamp, SortOrder.ASC)

        if (rows.toList().isEmpty()) {
            null
        } else {
            dev.scalar.trawler.server.graphql.FacetTimeSeries(
                facetType.name,
                urn = facet,
                points = rows.map { row ->
                    FacetTimeSeriesPoint(
                        row[FacetTimeSeries.timestamp],
                        if (facetType.metaType == FacetMetaType.INT) {
                            row[FacetTimeSeries.valueLong].toDouble()
                        } else {
                            row[FacetTimeSeries.valueDouble]
                        }
                    )
                }
            )
        }
    }
}
