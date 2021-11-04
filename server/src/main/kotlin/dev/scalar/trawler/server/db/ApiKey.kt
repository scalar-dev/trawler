package dev.scalar.trawler.server.db

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.timestamp

object ApiKey : UUIDTable("api_key") {
    val secret = text("secret")
    val description = text("description")
    val accountId = uuid("account_id").references(Account.id)
    val createdAt = timestamp("created_at")
}
