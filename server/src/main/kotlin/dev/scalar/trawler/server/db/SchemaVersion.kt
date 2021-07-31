package dev.scalar.trawler.server.db

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.timestamp

object SchemaVersion : UUIDTable("schema_version") {
    val createdAt = timestamp("created_at")
    val locator = text("locator")
    val datasetId = reference("dataset_id", Dataset.id)
}