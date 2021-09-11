package dev.scalar.trawler.server.graphql

import java.util.*

data class Facet(
    val entityId: UUID,
    val uri: String,
    val version: Long,
    val value: Any?,
    val metaType: String
)
