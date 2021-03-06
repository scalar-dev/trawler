package dev.scalar.trawler.server.db

import org.jetbrains.exposed.sql.Table

object AccountInfo : Table("account_info") {
    val accountId = uuid("account_id").references(Account.id)
    val email = text("email")
    val firstName = text("first_name")
    val lastName = text("last_name")
}
