package dev.scalar.trawler.server.collect

sealed class FacetSnapshotValue {
    data class String(val value: kotlin.String): FacetSnapshotValue()
    data class Id(val value: kotlin.String): FacetSnapshotValue()
}
