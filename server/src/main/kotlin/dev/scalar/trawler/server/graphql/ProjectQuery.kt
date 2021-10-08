package dev.scalar.trawler.server.graphql

import dev.scalar.trawler.server.db.AccountRole
import dev.scalar.trawler.server.db.Project
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class ProjectQuery {
    suspend fun projects(context: QueryContext) = newSuspendedTransaction {
        if (context.accountId == null) {
            throw Exception("Must be logged in")
        }

        Project
            .innerJoin(AccountRole)
            .select { AccountRole.accountId.eq(context.accountId) }
            .map {
                dev.scalar.trawler.server.graphql.Project(
                    it[Project.id].value,
                    it[Project.name]
                )
            }
    }
}
