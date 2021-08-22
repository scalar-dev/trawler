package dev.scalar.trawler.server.db

import org.jetbrains.exposed.dao.id.UUIDTable

object Entity : UUIDTable("entity") {
    val typeId = reference("type_id", EntityType.id)
    val urn = text("urn")
    val projectId = reference("project_id", Project.id)
}