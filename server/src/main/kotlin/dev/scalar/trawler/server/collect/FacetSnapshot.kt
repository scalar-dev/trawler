package dev.scalar.trawler.server.collect

import dev.scalar.trawler.ontology.FacetType
import java.util.UUID

data class FacetSnapshot(
    val projectId: UUID,
    val txId: UUID,
    val entityUrn: String,
    val facetType: FacetType,
    val values: List<FacetSnapshotValue>
)
