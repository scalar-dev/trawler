package dev.scalar.trawler.server.collect

import dev.scalar.trawler.server.schema.EntityType
import dev.scalar.trawler.server.schema.FacetType
import java.util.*

sealed class FacetSnapshotValue {
    data class String(val value: kotlin.String): FacetSnapshotValue()
    data class Id(val value: kotlin.String): FacetSnapshotValue()
}

sealed interface IngestOp {
    data class FacetSnapshot(
        val projectId: UUID,
        val entityUrn: String,
        val facetType: FacetType,
        val values: List<FacetSnapshotValue>
    ) : IngestOp

    data class EntitySnapshot(
        val projectId: UUID,
        val entityUrn: String,
        val entityType: EntityType
    )
}
