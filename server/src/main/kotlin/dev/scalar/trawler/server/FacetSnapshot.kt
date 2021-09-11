package dev.scalar.trawler.server

import dev.scalar.trawler.server.schema.FacetType

sealed class FacetSnapshotValue {
    data class String(val value: kotlin.String): FacetSnapshotValue()
    data class Id(val value: kotlin.String): FacetSnapshotValue()
}

data class FacetSnapshot(
    val entityUrn: String,
    val facetType: FacetType,
    val values: List<FacetSnapshotValue>
)
