package dev.scalar.trawler.server.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.scalar.trawler.server.App
import dev.scalar.trawler.server.KPostgreSQLContainer
import dev.scalar.trawler.server.auth.Users
import dev.scalar.trawler.server.auth.jwtAuth
import dev.scalar.trawler.server.auth.mintToken
import dev.scalar.trawler.server.collect.ApiKeyAuthProvider
import dev.scalar.trawler.server.db.devApiKey
import dev.scalar.trawler.server.db.devProject
import dev.scalar.trawler.server.db.devSecret
import dev.scalar.trawler.server.db.devUser
import dev.scalar.trawler.server.db.updateOntology
import dev.scalar.trawler.server.verticle.CollectApi
import dev.scalar.trawler.server.verticle.Config
import dev.scalar.trawler.server.verticle.GraphQLApi
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.web.common.WebEnvironment
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.extension.ExtendWith
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import javax.sql.DataSource

@Testcontainers
@ExtendWith(VertxExtension::class)
abstract class BaseVerticleTest {
    companion object {
        @Container
        @JvmStatic
        private val postgresContainer = KPostgreSQLContainer()
    }

    lateinit var dataSource: DataSource
    lateinit var apiKeyAuthProvider: ApiKeyAuthProvider
    lateinit var key: String

    fun initialSetup(vertx: Vertx) {
        System.setProperty(WebEnvironment.SYSTEM_PROPERTY_NAME, "dev")
        dataSource = App.configureDatabase(
            JsonObject(
                mapOf(
                    Config.PGPORT to postgresContainer.firstMappedPort,
                    Config.PGUSER to postgresContainer.username,
                    Config.PGPASSWORD to postgresContainer.password,
                    Config.PGDATABASE to postgresContainer.databaseName
                )
            )
        )

        apiKeyAuthProvider = ApiKeyAuthProvider(jdbcClient = JDBCClient.create(vertx, dataSource))

        runBlocking(vertx.dispatcher()) {
            devProject()
            devUser()
            updateOntology(vertx)
            key = devApiKey(apiKeyAuthProvider)
        }
    }

    fun deployCollectApi(vertx: Vertx, testContext: VertxTestContext) {
        val collectApi = CollectApi(apiKeyAuthProvider)

        vertx.deployVerticle(
            collectApi,
            DeploymentOptions(),
            testContext.succeedingThenComplete()
        )

        collectApi.awaitReady()
    }

    fun deployGraphQL(vertx: Vertx, testContext: VertxTestContext) {
        System.setProperty(WebEnvironment.SYSTEM_PROPERTY_NAME, "dev")

        val graphQL = GraphQLApi(dataSource)

        vertx.deployVerticle(
            graphQL,
            DeploymentOptions(),
            testContext.succeedingThenComplete()
        )

        graphQL.awaitReady()
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
