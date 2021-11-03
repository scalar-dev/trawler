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

    suspend fun listApiKeys(context: QueryContext): List<ApiKey> {
        if (context.user == null) {
           throw Exception("Must be logged in")
        }

        return newSuspendedTransaction {
            dev.scalar.trawler.server.db.ApiKey.select {
                dev.scalar.trawler.server.db.ApiKey.accountId.eq(context.accountId)
            }
                .map {
                    ApiKey(
                        it[dev.scalar.trawler.server.db.ApiKey.id].value,
                        it[dev.scalar.trawler.server.db.ApiKey.accountId],
                        it[dev.scalar.trawler.server.db.ApiKey.description],
                        null,
                        it[dev.scalar.trawler.server.db.ApiKey.createdAt]
                    )
                }
        }
    }
}
