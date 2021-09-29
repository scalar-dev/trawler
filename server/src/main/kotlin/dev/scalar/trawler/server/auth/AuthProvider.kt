package dev.scalar.trawler.server.auth

import io.vertx.core.Vertx
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions

fun jwtAuth(vertx: Vertx) = JWTAuth.create(
    vertx,
    JWTAuthOptions()
        .addPubSecKey(
            PubSecKeyOptions()
                .setAlgorithm("HS256")
                .setBuffer("keyboard cat")
        )
)
