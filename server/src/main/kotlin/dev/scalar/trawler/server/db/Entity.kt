package dev.scalar.trawler.server.db

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.timestamp

object Entity : UUIDTable("entity") {
    val urn = text("urn")
    val projectId = reference("project_id", Project.id)
    val createdAt = timestamp("created_at")
}