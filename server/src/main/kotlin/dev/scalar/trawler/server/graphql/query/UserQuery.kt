package dev.scalar.trawler.server.graphql.query

import dev.scalar.trawler.server.db.AccountInfo
import dev.scalar.trawler.server.db.AccountRole
import dev.scalar.trawler.server.graphql.ApiKey
import dev.scalar.trawler.server.graphql.QueryContext
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

data class User(
    val email: String,
    val firstName: String?,
    val lastName: String?
)

class UserQuery {
    suspend fun me(context: QueryContext): User? {
        if (context.user == null) {
            return null
        }

        val userId = UUID.fromString(context.user.principal().getString("sub"))

        return newSuspendedTransaction {
            AccountInfo.select {
                AccountInfo.accountId.eq(userId)
            }
                .map { User(it[AccountInfo.email], it[AccountInfo.firstName], it[AccountInfo.lastName]) }
                .first()
        }
    }

    suspend fun listApiKeys(context: QueryContext, project: String): List<ApiKey> {
        val projectId = context.projectId(project, AccountRole.ADMIN)

        return newSuspendedTransaction {
            dev.scalar.trawler.server.db.ApiKey.select {
                dev.scalar.trawler.server.db.ApiKey.projectId.eq(projectId)
            }
                .map {
                    ApiKey(
                        it[dev.scalar.trawler.server.db.ApiKey.id].value,
                        it[dev.scalar.trawler.server.db.ApiKey.projectId],
                        it[dev.scalar.trawler.server.db.ApiKey.description],
                        null,
                        it[dev.scalar.trawler.server.db.ApiKey.createdAt]
                    )
                }
        }
    }
}
