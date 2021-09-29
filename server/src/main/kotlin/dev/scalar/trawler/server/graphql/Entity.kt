package dev.scalar.trawler.server.graphql

import java.util.*

data class Entity(
    val entityId: UUID,
    val urn: String,
    val type: String,
    val typeName: String,
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

