package dev.scalar.trawler.server.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.scalar.trawler.server.KPostgreSQLContainer
import dev.scalar.trawler.server.auth.Users
import dev.scalar.trawler.server.auth.jwtAuth
import dev.scalar.trawler.server.auth.mintToken
import dev.scalar.trawler.server.db.devSecret
import dev.scalar.trawler.server.verticle.Config
import dev.scalar.trawler.server.verticle.GraphQLApi
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.common.WebEnvironment
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.coroutines.await
import org.junit.jupiter.api.extension.ExtendWith
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@ExtendWith(VertxExtension::class)
abstract class BaseVerticleTest {
    companion object {
        @Container
        @JvmStatic
        private val postgresContainer = KPostgreSQLContainer()
    }

    fun deployGraphQL(vertx: Vertx, testContext: VertxTestContext) {
        System.setProperty(WebEnvironment.SYSTEM_PROPERTY_NAME, "dev")

        vertx.deployVerticle(
            GraphQLApi(),
            DeploymentOptions().setConfig(
                JsonObject(
                    mapOf(
                        Config.PGPORT to postgresContainer.firstMappedPort,
                        Config.PGUSER to postgresContainer.username,
                        Config.PGPASSWORD to postgresContainer.password,
                        Config.PGDATABASE to postgresContainer.databaseName
                    )
                )
            ).setWorker(true),
            testContext.succeedingThenComplete()
        )
    }

    protected suspend fun sendRequest(vertx: Vertx, port: Int, path: String, body: String): HttpClientResponse {
        val jwt = jwtAuth(vertx, devSecret(), 30)
        val client = vertx.createHttpClient()
        val request = client
            .request(HttpMethod.POST, port, "localhost", path)
            .await()

        return request
            .putHeader("Authorization", "Bearer ${mintToken(jwt, Users.DEV)}")
            .send(body)
            .await()
    }

    protected suspend fun graphQLRequest(vertx: Vertx, query: String, variables: Map<String, Any> = emptyMap()): GraphQLResponse {
        val response = sendRequest(
            vertx,
            8080,
            "/graphql",
            jacksonObjectMapper().writeValueAsString(
                mapOf(
                    "query" to query,
                    "variables" to variables
                )
            )
        )

        val body = response.body().await()
        return jacksonObjectMapper().readValue(body.bytes)
    }
}
