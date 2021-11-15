package dev.scalar.trawler.server.graphql.query

import dev.scalar.trawler.server.db.AccountRole
import dev.scalar.trawler.server.db.Project
import dev.scalar.trawler.server.graphql.QueryContext
import dev.scalar.trawler.server.graphql.Unauthenticated
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class ProjectQuery {
    @Unauthenticated
    suspend fun projects(context: QueryContext) = newSuspendedTransaction {
        Project
            .innerJoin(AccountRole)
            .select { AccountRole.accountId.eq(context.accountId) and Project.slug.isNotNull() }
            .map {
                dev.scalar.trawler.server.graphql.type.Project(
                    it[Project.id].value,
                    it[Project.name],
                    it[Project.slug]
                )
            }
    }
}
