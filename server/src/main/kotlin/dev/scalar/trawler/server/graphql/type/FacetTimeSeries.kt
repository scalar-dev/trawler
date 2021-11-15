package dev.scalar.trawler.server.graphql

import java.time.Instant

data class FacetTimeSeriesPoint(
    val timestamp: Instant,
    // TODO: Fix to better type
    val value: Double
)

data class FacetTimeSeries(
    val name: String,
    val urn: String,
    val points: List<FacetTimeSeriesPoint>
)
