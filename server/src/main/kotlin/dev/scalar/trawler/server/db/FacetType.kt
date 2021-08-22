package dev.scalar.trawler.server.db

import org.jetbrains.exposed.dao.id.UUIDTable

object FacetType : UUIDTable("facet_type") {
    val name = text("name")
    val uri = text("uri")
    val metaType = text("meta_type")
    val projectId = reference("project_id", Project.id)

    enum class MetaType(val value: String) {
        STRING("string"),
        INT("int"),
        DOUBLE("double"),
        RELATIONSHIP("relationship"),
        RELATIONSHIP_OWNED("relationship_owned")
    }
}