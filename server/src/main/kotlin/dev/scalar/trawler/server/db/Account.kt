package dev.scalar.trawler.server.db

import org.jetbrains.exposed.dao.id.UUIDTable

object Account : UUIDTable("account") {
    val password = text("password")
}
