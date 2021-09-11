package dev.scalar.trawler.server.collect

import com.fasterxml.jackson.annotation.JsonValue
import dev.scalar.trawler.server.collect.CollectNode

data class CollectRequest(
    @get:JsonValue val nodes: List<CollectNode>
)