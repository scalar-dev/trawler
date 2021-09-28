package dev.scalar.trawler.server.graphql

import dev.scalar.trawler.server.db.FacetLog
import org.jetbrains.exposed.sql.select
import java.util.*

data class Entity(
    val entityId: UUID,
    val type: String,
    val facets: List<Facet>
) {
//    fun facetLog(entityId: UUID, facets: List<UUID>?) {
//        FacetLog.select {
//            if (facets != null) {
//                FacetLog.typeId.inList(facets)
//            }
//
//            FacetLog.entityId.eq(entityId)
//        }.map {
//            FacetLog
//
//        }
//    }
}

