package dev.scalar.trawler.server.db

import org.jetbrains.exposed.dao.id.UUIDTable

object Project : UUIDTable("project") {
    val name = text("name")
}
