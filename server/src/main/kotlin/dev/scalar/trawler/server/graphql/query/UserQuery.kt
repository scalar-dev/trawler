package dev.scalar.trawler.server.graphql.query

import dev.scalar.trawler.server.db.AccountInfo
import dev.scalar.trawler.server.graphql.type.ApiKey
import dev.scalar.trawler.server.graphql.QueryContext
import dev.scalar.trawler.server.graphql.type.User
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class UserQuery {
    suspend fun me(context: QueryContext): User? {
        val userId = UUID.fromString(context.user!!.principal().getString("sub"))

        return newSuspendedTransaction {
            AccountInfo.select {
                AccountInfo.accountId.eq(userId)
            }
                .map { User(it[AccountInfo.email], it[AccountInfo.firstName], it[AccountInfo.lastName]) }
                .first()
        }
    }

    suspend fun apiKeys(context: QueryContext): List<ApiKey> {
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
