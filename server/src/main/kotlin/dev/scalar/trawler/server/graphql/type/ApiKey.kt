package dev.scalar.trawler.server.graphql.type

import java.time.Instant
import java.util.UUID

data class ApiKey(
    val id: UUID,
    val accountId: UUID,
    val description: String?,
    val secret: String?,
    val createdAt: Instant
)
