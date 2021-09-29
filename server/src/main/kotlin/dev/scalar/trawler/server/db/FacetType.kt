package dev.scalar.trawler.server.db

import org.jetbrains.exposed.dao.id.UUIDTable

object FacetType : UUIDTable("facet_type") {
    val projectId = reference("project_id", Project.id).nullable()

    val name = text("name")
    val uri = text("uri")
    val metaType = text("meta_type")
//    val jsonSchema = text("json_schema").nullable()
    val isDeprecated = bool("is_deprecated")
}
