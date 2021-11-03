package dev.scalar.trawler.server.verticle

import com.fasterxml.jackson.datatype.jsonp.JSONPModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import dev.scalar.trawler.server.auth.jwtAuth
import dev.scalar.trawler.server.db.devSecret
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.kotlin.coroutines.CoroutineVerticle
import org.apache.logging.log4j.LogManager

abstract class BaseVerticle : CoroutineVerticle() {
    private val log = LogManager.getLogger()

    protected val jwtAuth by lazy {
        jwtAuth(
            vertx,
            config.getString(Config.TRAWLER_SECRET) ?: devSecret(),
            30 * 24 * 60 * 60
        )
    }

    override suspend fun start() {
        DatabindCodec.mapper()
            .registerModule(KotlinModule())
            .registerModule(JSONPModule())
    }
}
