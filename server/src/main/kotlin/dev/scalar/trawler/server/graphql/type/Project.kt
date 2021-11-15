package dev.scalar.trawler.server.graphql.type

import java.util.UUID

data class Project(
    val id: UUID,
    val name: String,
    val slug: String
)
