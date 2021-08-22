package dev.scalar.trawler.server

import java.util.UUID


sealed class FacetSnapshotValue {
    data class String(val value: kotlin.String): FacetSnapshotValue()
    data class Id(val value: kotlin.String): FacetSnapshotValue()
}

data class FacetSnapshot(
    val entityId: UUID,
    val facetTypeId: UUID,
    val values: List<FacetSnapshotValue>
)
