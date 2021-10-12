package dev.scalar.trawler.server

import dev.scalar.trawler.server.auth.jwtAuth
import dev.scalar.trawler.server.db.Project
import dev.scalar.trawler.server.db.devUserToken
import dev.scalar.trawler.server.verticle.CollectApi
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.common.WebEnvironment
import io.vertx.junit5.VertxExtension
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@ExtendWith(VertxExtension::class)
class CollectEndpointShould {
    companion object {
        @Container
        @JvmStatic
        private val postgresContainer = KPostgreSQLContainer()
    }

    @Test
    fun ingest_json_ld(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
        System.setProperty(WebEnvironment.SYSTEM_PROPERTY_NAME, "dev")
        vertx.deployVerticle(
            CollectApi(),
            DeploymentOptions().setConfig(
                JsonObject(
                    mapOf(
                        "PGPORT" to postgresContainer.firstMappedPort,
                        "PGUSER" to "test",
                        "PGPASSWORD" to "test"
                    )
                )
            )
        ).await()

        val jwt = jwtAuth(vertx)
        val client = vertx.createHttpClient()
        val request = client
            .request(HttpMethod.POST, 9090, "localhost", "/api/collect/${Project.DEMO_PROJECT_ID}")
            .await()
        val response = request
            .putHeader("Authorization", "Bearer ${devUserToken(jwt)}")
            .send(
                """
                {"@context": "http://trawler.dev/schema/core", "@graph": [{"@type": "tr:SqlDatabase", "@id": "urn:tr:::postgres/example.com/postgres", "name": "foo", "tr:has": [{"@type": "tr:SqlTable", "@id": "urn:tr:::postgres/example.com/postgres/foo", "name": "foo"}]}]}
                """
            )
            .await()

        Assert.assertEquals(200, response.statusCode())

        val buffer = response.body().await()
        Assert.assertNotNull(buffer)
    }
}
