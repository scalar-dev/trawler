package dev.scalar.trawler.server.db

import org.jetbrains.exposed.sql.Table

object AccountRole : Table("account_role") {
    val accountId = uuid("account_id").references(Account.id)
    val projectId = uuid("project_id").references(Project.id)
    val role = text("role")
}
