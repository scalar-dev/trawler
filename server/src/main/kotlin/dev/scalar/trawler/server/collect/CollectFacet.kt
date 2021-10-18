package dev.scalar.trawler.server.collect

import com.fasterxml.jackson.annotation.JsonProperty

data class CollectFacet(
    @JsonProperty("@id")
    val id: String?,

    @JsonProperty("@value")
    val value: Any?,

    @JsonProperty("@type")
    val type: String?,
)
