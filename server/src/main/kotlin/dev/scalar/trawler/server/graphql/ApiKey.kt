package dev.scalar.trawler.server.graphql

import java.time.Instant
import java.util.UUID

data class ApiKey(
    val id: UUID,
    val projectId: UUID,
    val description: String?,
    val secret: String?,
    val createdAt: Instant
)
