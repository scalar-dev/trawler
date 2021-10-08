package dev.scalar.trawler.server.graphql

import dev.scalar.trawler.server.db.AccountInfo
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

data class User(
    val email: String
)

class UserQuery {
    suspend fun me(context: QueryContext): User {
        if (context.user == null) {
            throw Exception("Not logged in")
        }

        val userId = UUID.fromString(context.user.principal().getString("sub"))

        return newSuspendedTransaction {
            AccountInfo.select {
                AccountInfo.accountId.eq(userId)
            }
                .map { User(it[AccountInfo.email]) }
                .first()
        }
    }
}
