package dev.scalar.trawler.server.graphql

data class Facet(
    val uri: String,
    val name: String,
    val metaType: String,
    val version: Long,
    val value: Any?,
)
