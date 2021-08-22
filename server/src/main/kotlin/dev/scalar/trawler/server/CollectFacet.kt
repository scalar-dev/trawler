package dev.scalar.trawler.server

import com.fasterxml.jackson.annotation.JsonProperty

data class CollectFacet(
    @JsonProperty("@id")
    val id: String?,

    @JsonProperty("@value")
    val value: String?
)