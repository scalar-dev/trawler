package dev.scalar.trawler.server.collect

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.User
import io.vertx.ext.auth.authentication.Credentials
import io.vertx.ext.auth.authentication.TokenCredentials
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials
import io.vertx.ext.auth.jdbc.JDBCAuthenticationOptions
import io.vertx.ext.auth.jdbc.impl.JDBCAuthenticationImpl
import io.vertx.ext.jdbc.JDBCClient

class ApiKeyAuthProvider(jdbcClient: JDBCClient) : JDBCAuthenticationImpl(
    jdbcClient,
    JDBCAuthenticationOptions().setAuthenticationQuery("SELECT secret FROM api_key WHERE secret = ?")
) {
    companion object {
        val SALT = "NOSALT"
    }

    override fun authenticate(credentials: JsonObject, resultHandler: Handler<AsyncResult<User>>) {
        authenticate(TokenCredentials(credentials), resultHandler)
    }

    override fun authenticate(credentials: Credentials, resultHandler: Handler<AsyncResult<User>>) {
        val tokenCredentials = credentials as TokenCredentials
        val hash = hash(
            "pbkdf2",
            SALT,
            tokenCredentials.token
        )
        super.authenticate(UsernamePasswordCredentials(hash, tokenCredentials.token), resultHandler)
    }
}
