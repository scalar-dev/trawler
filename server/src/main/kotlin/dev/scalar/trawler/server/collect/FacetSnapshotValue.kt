package dev.scalar.trawler.server.collect

sealed class FacetSnapshotValue {
    data class Value(val value: Any): FacetSnapshotValue()
    data class Id(val value: kotlin.String): FacetSnapshotValue()
}
