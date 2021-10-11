package dev.scalar.trawler.server

import dev.scalar.trawler.server.auth.jwtAuth
import dev.scalar.trawler.server.db.Project
import dev.scalar.trawler.server.db.devUserToken
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.junit5.VertxExtension
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testcontainers.junit.jupiter.Container

@ExtendWith(VertxExtension::class)
class CollectEndpointShould {
    companion object {
        @Container
        @JvmStatic
        private val postgresContainer = KPostgreSQLContainer()
    }

    @Test
    fun ingest_json_ld(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
        vertx.deployVerticle(App()).await()
        val jwt = jwtAuth(vertx)
        val client = vertx.createHttpClient()
        val request = client
            .request(HttpMethod.POST, 9090, "localhost", "/api/collect/${Project.DEMO_PROJECT_ID}")
            .await()
        val response = request
            .putHeader("Authorization", "Bearer ${devUserToken(jwt)}")
            .send("{}")
            .await()

        Assert.assertEquals(200, response.statusCode())

        val buffer = response.body().await()
        Assert.assertNotNull(buffer)
    }
}
