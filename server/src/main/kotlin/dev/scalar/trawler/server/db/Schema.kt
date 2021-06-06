package dev.scalar.trawler.server.db

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.timestamp

object Schema : UUIDTable("schema") {
    val createdAt = timestamp("created_at")
    val locator = text("locator")
}