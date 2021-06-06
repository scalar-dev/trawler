package dev.scalar.trawler.server.db

import org.jetbrains.exposed.dao.id.UUIDTable

object SchemaField : UUIDTable("schema_field") {
    val name = text("name")
    val type = text("type")
    val isNullable = bool("is_nullable")
    val isPrimaryKey = bool("is_primary_key")
    val comment = text("comment")

    val schemaObjectId = reference("schema_object_id", SchemaObject.id)
}