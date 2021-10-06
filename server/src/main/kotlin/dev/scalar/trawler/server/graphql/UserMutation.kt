package dev.scalar.trawler.server.graphql

import dev.scalar.trawler.server.db.Account
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials
import io.vertx.kotlin.coroutines.await
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.security.SecureRandom
import java.util.*

data class AuthenticatedUser(
    val jwt: String
)

class UserMutation() {
    suspend fun createUser(context: QueryContext, username: String, password: String): UUID {
        val salt = ByteArray(32)
        SecureRandom().nextBytes(salt)

        val hash = context.jdbcAuthentication.hash(
            "pbkdf2", // hashing algorithm
            Base64.getEncoder().encodeToString(salt),
            password
        )

        return newSuspendedTransaction {
            Account.insertAndGetId {
                it[Account.username] = username
                it[Account.password] = hash
            }.value
        }
    }

    suspend fun login(context: QueryContext, username: String, password: String): AuthenticatedUser {
        val user = context.jdbcAuthentication.authenticate(
            UsernamePasswordCredentials(
                username,
                password
            )
        ).await()

        val claims = mapOf(
            "sub" to username
        )

        return AuthenticatedUser(
            context.jwtAuth.generateToken(JsonObject(claims))
        )
    }
}
