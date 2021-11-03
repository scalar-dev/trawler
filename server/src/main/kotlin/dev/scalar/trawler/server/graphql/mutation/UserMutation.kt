package dev.scalar.trawler.server.graphql

import dev.scalar.trawler.server.db.Account
import dev.scalar.trawler.server.db.AccountInfo
import dev.scalar.trawler.server.db.AccountRole
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials
import io.vertx.kotlin.coroutines.await
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID

data class AuthenticatedUser(
    val jwt: String
)

class UserMutation() {
    suspend fun createUser(context: QueryContext, email: String, password: String): UUID {
        val salt = ByteArray(32)
        SecureRandom().nextBytes(salt)

        val hash = context.jdbcAuthentication.hash(
            "pbkdf2", // hashing algorithm
            Base64.getEncoder().encodeToString(salt),
            password
        )

        return newSuspendedTransaction {
            val accountId = Account.insertAndGetId {
                it[Account.password] = hash
            }.value

            AccountInfo.insert {
                it[AccountInfo.email] = email
                it[AccountInfo.accountId] = accountId
            }

            accountId
        }
    }

    suspend fun login(context: QueryContext, email: String, password: String): AuthenticatedUser {
        val userId = newSuspendedTransaction {
            AccountInfo.select { AccountInfo.email eq email }
                .map { it[AccountInfo.accountId].toString() }.firstOrNull()
                ?: throw Exception("Invalid username/password")
        }

        val user = context.jdbcAuthentication.authenticate(
            UsernamePasswordCredentials(
                userId,
                password
            )
        ).await()

        val claims = mapOf(
            "sub" to userId
        )

        return AuthenticatedUser(
            context.jwtAuth.generateToken(JsonObject(claims))
        )
    }

    suspend fun createApiKey(context: QueryContext, project: String): ApiKey = newSuspendedTransaction {
        val projectId = context.projectId(project, AccountRole.ADMIN)
        val key = context.apiAuth.makeRandomKey()

        val keyId = dev.scalar.trawler.server.db.ApiKey.insertAndGetId {
            it[accountId] = context.accountId
            it[secret] = key.hash
        }.value

        dev.scalar.trawler.server.db.ApiKey.select { dev.scalar.trawler.server.db.ApiKey.id.eq(keyId) }
            .map {
                ApiKey(
                    id = keyId,
                    accountId = context.accountId,
                    description = null,
                    secret = key.key,
                    createdAt = it[dev.scalar.trawler.server.db.ApiKey.createdAt]
                )
            }
            .first()
    }
}
