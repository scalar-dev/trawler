package dev.scalar.trawler.server.graphql

import java.util.*

data class Entity(
    val entityId: UUID,
    val type: String,
    val facets: List<Facet>
)
