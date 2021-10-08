package dev.scalar.trawler.server.db

import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

object Project : UUIDTable("project") {
    val name = text("name")

    val DEMO_PROJECT_ID = UUID.fromString("63255f7a-e383-457a-9c30-4c7f95308749")
}
