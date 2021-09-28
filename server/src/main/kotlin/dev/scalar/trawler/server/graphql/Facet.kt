package dev.scalar.trawler.server.graphql

import java.util.*

data class Facet(
    val uri: String,
    val name: String,
    val metaType: String,
    val version: Long,
    val value: Any?,
)
