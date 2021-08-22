package dev.scalar.trawler.server.db

import org.jetbrains.exposed.dao.id.UUIDTable

object Facet : UUIDTable("facet") {
    val entityId = reference("entity_id", Entity.id)
    val typeId = reference("type_id", FacetType.id)
    val latestVersion = long("latest_version")
}