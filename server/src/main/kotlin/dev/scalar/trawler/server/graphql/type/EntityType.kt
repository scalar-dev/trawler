package dev.scalar.trawler.server.graphql.type

import java.util.*

data class EntityType(val id: UUID, val uri: String, val name: String, val isRootType: Boolean)
