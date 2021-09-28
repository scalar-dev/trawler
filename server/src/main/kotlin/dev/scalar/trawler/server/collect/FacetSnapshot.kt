package dev.scalar.trawler.server.collect

import dev.scalar.trawler.server.ontology.FacetType
import java.util.*


data class FacetSnapshot(
    val projectId: UUID,
    val txId: UUID,
    val entityUrn: String,
    val facetType: FacetType,
    val values: List<FacetSnapshotValue>
)
