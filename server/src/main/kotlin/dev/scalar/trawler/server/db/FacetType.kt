package dev.scalar.trawler.server.db

import org.jetbrains.exposed.dao.id.UUIDTable

object FacetType : UUIDTable("facet_type") {
    val projectId = reference("project_id", Project.id)

    val name = text("name")
    val uri = text("uri")
    val metaType = text("meta_type")
//    val jsonSchema = text("json_schema").nullable()

    enum class MetaType(val value: String) {
        STRING("string"),
        BOOLEAN("boolean"),
        INT("int"),
        DOUBLE("double"),
        JSON("json"),
        RELATIONSHIP("relationship"),
        TYPE_REFERENCE("type_reference")
    }
}