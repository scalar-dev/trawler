package dev.scalar.trawler.server.auth

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.JWTOptions
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import java.util.UUID

object Users {
    val GUEST = UUID(0, 0)
    val DEV = UUID(0, 1)
}

fun jwtAuth(vertx: Vertx, secret: String, expiresInSeconds: Int) = JWTAuth.create(
    vertx,
    JWTAuthOptions()
        .addPubSecKey(
            PubSecKeyOptions()
                .setAlgorithm("HS256")
                .setBuffer(secret)
        )
        .setJWTOptions(JWTOptions().setExpiresInSeconds(expiresInSeconds))
)

fun mintToken(jwtAuth: JWTAuth, userId: UUID, scopes: List<String> = emptyList()) = jwtAuth.generateToken(
    JsonObject(
        mapOf(
            "sub" to userId.toString(),
            "scope" to scopes.joinToString(" ")
        )
    )
)
