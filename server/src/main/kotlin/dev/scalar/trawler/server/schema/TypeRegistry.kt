package dev.scalar.trawler.server.schema

import java.util.*

interface TypeRegistry {
    suspend fun entityTypeByUri(uri: String): EntityType?
    suspend fun facetTypeByUri(uri: String): FacetType?
    suspend fun facetTypeById(id: UUID): FacetType?

    companion object {
        val ENTITY_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
    }
}
