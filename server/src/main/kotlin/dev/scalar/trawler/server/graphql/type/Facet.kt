package dev.scalar.trawler.server.graphql.type

data class Facet(
    val uri: String,
    val name: String,
    val metaType: String,
    val version: Long,
    val value: Any?,
)
