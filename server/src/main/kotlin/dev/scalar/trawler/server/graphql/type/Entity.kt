package dev.scalar.trawler.server.graphql.type

import dev.scalar.trawler.ontology.FacetMetaType
import dev.scalar.trawler.server.db.Entity
import dev.scalar.trawler.server.db.FacetLog
import dev.scalar.trawler.server.db.FacetTimeSeries
import dev.scalar.trawler.server.db.FacetType
import dev.scalar.trawler.server.graphql.FacetTimeSeriesPoint
import dev.scalar.trawler.server.graphql.QueryContext
import dev.scalar.trawler.server.graphql.Unauthenticated
import dev.scalar.trawler.server.graphql.fetchEntities
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

data class Entity(
    val id: UUID,
    val projectId: UUID,
    val urn: String,
    val type: String,
    val typeName: String,
    val facets: List<Facet>
) {
    @Unauthenticated
    suspend fun facetLog(context: QueryContext, facets: List<String>): List<dev.scalar.trawler.server.graphql.type.FacetLog> = newSuspendedTransaction {
        val rows = FacetLog
            .join(Entity, JoinType.INNER, FacetLog.entityUrn, Entity.urn)
            .join(FacetType, JoinType.INNER, FacetLog.typeId, FacetType.id)
            .select {
                FacetLog.entityId.eq(this@Entity.id) and FacetType.uri.inList(facets)
            }
            .orderBy(FacetLog.version, SortOrder.DESC)

        val urns = rows.flatMap { row ->
            val values = row[FacetLog.value]
            values?.map { it.toString() } ?: emptyList<String>()
        }

        val entities = Entity
            .select { Entity.urn.inList(urns) }
            .map { row -> row[Entity.id].value }

        val entitiesByUrn = fetchEntities(context.accountId, entities)
            .associateBy { it.urn }

        rows.map { row ->
            val facetType = context.ontologyCache.get(projectId).facetTypeById(row[FacetLog.typeId].value)!!

            FacetLog(
                row[FacetLog.id].value,
                facetType.name,
                facetType.uri,
                row[FacetLog.version],
                row[FacetLog.value]?.mapNotNull { entitiesByUrn[it.toString()] } ?: emptyList(),
                row[FacetLog.createdAt]
            )
        }
    }

    @Unauthenticated
    suspend fun timeSeries(context: QueryContext, facet: String): dev.scalar.trawler.server.graphql.FacetTimeSeries? = newSuspendedTransaction {
        val facetType = context.ontologyCache.get(projectId).facetTypeByUri(facet)!!

        val rows = FacetTimeSeries
            .join(FacetType, JoinType.INNER, FacetTimeSeries.typeId, FacetType.id)
            .select {
                FacetTimeSeries.entityId.eq(this@Entity.id) and FacetType.uri.eq(facet)
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
