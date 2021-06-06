package dev.scalar.trawler.server.db

import org.jetbrains.exposed.dao.id.UUIDTable

object SchemaObject : UUIDTable("schema_object") {
    val name = text("name")
    val comment = text("comment")
    val schemaId = reference("schema_id", Schema.id)
}