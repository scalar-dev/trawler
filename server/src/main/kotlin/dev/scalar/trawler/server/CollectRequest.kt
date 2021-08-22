package dev.scalar.trawler.server

import com.fasterxml.jackson.annotation.JsonValue

data class CollectRequest(
    @get:JsonValue val nodes: List<CollectNode>
)