package dev.scalar.trawler.server.db

import org.jetbrains.exposed.dao.id.UUIDTable

object EntityType : UUIDTable("entity_type") {
    val name = text("name")
    val uri = text("uri")
    val projectId =  optReference("project_id", Project.id)
    val isDeprecated = bool("is_deprecated")
}