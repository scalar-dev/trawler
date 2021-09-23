package dev.scalar.trawler.server.ontology

import java.util.*

data class EntityType(
    val uri: String,
    val id: UUID,
    val name: String,
    val allowedFacets: Set<FacetType>
)