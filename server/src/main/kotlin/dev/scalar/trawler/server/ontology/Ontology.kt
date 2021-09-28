package dev.scalar.trawler.server.ontology

import java.util.*

interface Ontology {
    fun entityTypeByUri(uri: String): EntityType?
    fun facetTypeByUri(uri: String): FacetType?
    fun facetTypeById(id: UUID): FacetType?

    companion object {
        val ENTITY_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
        val NAME_TYPE = "http://schema.org/name"
    }
}
