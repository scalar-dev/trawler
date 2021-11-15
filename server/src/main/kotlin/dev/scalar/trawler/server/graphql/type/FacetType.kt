package dev.scalar.trawler.server.graphql.type

import java.util.*

data class FacetType(
    val id: UUID,
    val uri: String,
    val name: String,
    val metaType: String,
    val isRootType: Boolean,
    val indexTimeSeries: Boolean,
    val jsonSchema: Any?
)