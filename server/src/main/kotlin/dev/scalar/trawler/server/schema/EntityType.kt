package dev.scalar.trawler.server.schema

import java.util.*

data class EntityType(
    val uri: String,
    val id: UUID,
    val name: String,
    val allowedFacets: Set<FacetType>
)