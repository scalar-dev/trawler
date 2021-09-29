package dev.scalar.trawler.server.collect

import com.fasterxml.jackson.annotation.JsonValue

data class CollectRequest(
    @get:JsonValue val nodes: List<CollectNode>
)
