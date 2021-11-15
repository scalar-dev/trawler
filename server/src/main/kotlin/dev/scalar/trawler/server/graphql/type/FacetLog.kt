package dev.scalar.trawler.server.graphql.type

import dev.scalar.trawler.server.graphql.type.Entity
import java.time.Instant
import java.util.UUID

data class FacetLog(
    val id: UUID,
    val name: String,
    val urn: String,
    val version: Long,
    val entities: List<Entity>,
    val createdAt: Instant
)
