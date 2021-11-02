package dev.scalar.trawler.server.util

data class GraphQLResponse(
    val data: Map<String, Any>,
    val errors: List<Map<String, Any>>? = null
)
