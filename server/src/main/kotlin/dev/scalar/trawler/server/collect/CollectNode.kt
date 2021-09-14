package dev.scalar.trawler.server.collect

import com.fasterxml.jackson.annotation.*
import dev.scalar.trawler.server.collect.CollectFacet

data class CollectNode(
    @JsonProperty("@id")
    val id: String,

    @JsonProperty("@type")
    val type: List<String>
) {
    @JsonIgnore
    private val properties = LinkedHashMap<String, List<CollectFacet>>()

    @JsonAnySetter
    fun set(name: String, value: List<CollectFacet>) {
        properties.put(name, value)
    }

    @JsonAnyGetter
    fun facets(): Map<String, List<CollectFacet>> {
        return properties
    }
}