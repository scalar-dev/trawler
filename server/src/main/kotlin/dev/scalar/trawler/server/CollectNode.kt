package dev.scalar.trawler.server

import com.fasterxml.jackson.annotation.*

data class CollectNode(
    @JsonProperty("@id")
    val id: String,

    @JsonProperty("@type")
    val type: List<String>
) {
    private @JsonIgnore
    val properties = LinkedHashMap<String, List<CollectFacet>>()

    @JsonAnySetter
    fun set(name: String, value: List<CollectFacet>) {
        properties.put(name, value)
    }

    @JsonAnyGetter
    fun facets(): Map<String, List<CollectFacet>> {
        return properties
    }
}