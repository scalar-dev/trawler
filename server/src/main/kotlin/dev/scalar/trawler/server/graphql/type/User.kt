package dev.scalar.trawler.server.graphql.type

data class User(
    val email: String,
    val firstName: String?,
    val lastName: String?
)