package dev.scalar.trawler.server.ontology

import java.util.*

interface Ontology {
    suspend fun entityTypeByUri(uri: String): EntityType?
    suspend fun facetTypeByUri(uri: String): FacetType?
    suspend fun facetTypeById(id: UUID): FacetType?

    companion object {
        val ENTITY_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
    }
}
