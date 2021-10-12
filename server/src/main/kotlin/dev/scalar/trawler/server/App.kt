package dev.scalar.trawler.server

import dev.scalar.trawler.server.verticle.CollectApi
import dev.scalar.trawler.server.verticle.GraphQLApi
import dev.scalar.trawler.server.verticle.Indexer
import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.DeploymentOptions
import io.vertx.core.Verticle
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitResult
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class App : CoroutineVerticle() {
    private val log: Logger = LogManager.getLogger()

    private inline fun <reified T : Verticle> deployVerticle(config: JsonObject, worker: Boolean) = vertx
        .deployVerticle(T::class.java, DeploymentOptions().setWorker(worker).setConfig(config))
        .onFailure { log.error("Failed to deploy ${T::class.qualifiedName}", it) }

    private fun configRetriever() = ConfigRetriever.create(
        vertx,
        ConfigRetrieverOptions()
            .addStore(ConfigStoreOptions().setType("env"))
    )

    override suspend fun start() {
        val config = awaitResult<JsonObject> { h -> configRetriever().getConfig(h) }

        deployVerticle<CollectApi>(config, true)
        deployVerticle<Indexer>(config, true)
        deployVerticle<GraphQLApi>(config, false)
    }
}
