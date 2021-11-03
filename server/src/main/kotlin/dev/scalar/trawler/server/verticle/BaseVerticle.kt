package dev.scalar.trawler.server.verticle

import com.fasterxml.jackson.datatype.jsonp.JSONPModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import dev.scalar.trawler.server.auth.jwtAuth
import dev.scalar.trawler.server.db.devSecret
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.kotlin.coroutines.CoroutineVerticle
import org.apache.logging.log4j.LogManager
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

abstract class BaseVerticle : CoroutineVerticle() {
    private val log = LogManager.getLogger()
    val readyLatch = CountDownLatch(1)

    protected val jwtAuth by lazy {
        jwtAuth(
            vertx,
            config.getString(Config.TRAWLER_SECRET) ?: devSecret(),
            30 * 24 * 60 * 60
        )
    }

    fun awaitReady() = readyLatch.await(5, TimeUnit.SECONDS)

    override suspend fun start() {
        DatabindCodec.mapper()
            .registerModule(KotlinModule())
            .registerModule(JSONPModule())
    }
}
