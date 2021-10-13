package dev.scalar.trawler.server

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.scalar.trawler.server.auth.Users
import dev.scalar.trawler.server.auth.jwtAuth
import dev.scalar.trawler.server.auth.mintToken
import dev.scalar.trawler.server.db.Account
import dev.scalar.trawler.server.db.FacetLog
import dev.scalar.trawler.server.db.Project
import dev.scalar.trawler.server.db.devSecret
import dev.scalar.trawler.server.verticle.CollectApi
import dev.scalar.trawler.server.verticle.Config
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.common.WebEnvironment
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.Random
import java.util.UUID

@Testcontainers
@ExtendWith(VertxExtension::class)
class CollectEndpointShould {
    companion object {
        @Container
        @JvmStatic
        private val postgresContainer = KPostgreSQLContainer()
    }

    @BeforeEach
    fun deployVerticle(vertx: Vertx, testContext: VertxTestContext) {
        System.setProperty(WebEnvironment.SYSTEM_PROPERTY_NAME, "dev")
        vertx.deployVerticle(
            CollectApi(),
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

    private suspend fun sendRequest(vertx: Vertx, body: String): HttpClientResponse {
        val jwt = jwtAuth(vertx, devSecret(), 30)
        val client = vertx.createHttpClient()
        val request = client
            .request(HttpMethod.POST, 9090, "localhost", "/api/collect/${Project.DEMO_PROJECT_ID}")
            .await()

        return request
            .putHeader("Authorization", "Bearer ${mintToken(jwt, Users.DEV, listOf("collect"))}")
            .send(body)
            .await()
    }

    @Test
    fun reject_bad_jwt(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
        val jwt = jwtAuth(vertx, "the wrong secret", 30)
        val client = vertx.createHttpClient()
        val request = client
            .request(HttpMethod.POST, 9090, "localhost", "/api/collect/${Project.DEMO_PROJECT_ID}")
            .await()

        val response = request
            .putHeader("Authorization", "Bearer ${mintToken(jwt, Users.DEV, listOf("collect"))}")
            .send("{}")
            .await()

        Assert.assertEquals(401, response.statusCode())
    }

    @Test
    fun reject_expired_jwt(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
        val jwt = jwtAuth(vertx, devSecret(), 0)
        val client = vertx.createHttpClient()
        val request = client
            .request(HttpMethod.POST, 9090, "localhost", "/api/collect/${Project.DEMO_PROJECT_ID}")
            .await()

        val token = jwt.generateToken(
            JsonObject(
                mapOf(
                    "sub" to Users.DEV.toString(),
                    "scope" to listOf("collect").joinToString(" "),
                    "exp" to 0
                )
            )
        )

        val response = request
            .putHeader("Authorization", "Bearer $token")
            .send("{}")
            .await()

        Assert.assertEquals(401, response.statusCode())
    }

    @Test
    fun reject_no_jwt(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
        val client = vertx.createHttpClient()
        val request = client
            .request(HttpMethod.POST, 9090, "localhost", "/api/collect/${Project.DEMO_PROJECT_ID}")
            .await()

        val response = request
            .send("{}")
            .await()

        Assert.assertEquals(401, response.statusCode())
    }

    @Test
    fun reject_no_authz(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
        val jwt = jwtAuth(vertx, devSecret(), 30)

        newSuspendedTransaction {
            Account.insertIgnore {
                it[Account.id] = UUID(0, 2)
                it[Account.password] = "NO_LOGIN"
            }
        }

        val token = jwt.generateToken(
            JsonObject(
                mapOf(
                    "sub" to UUID(0, 2).toString(),
                    "scope" to listOf("collect").joinToString(" "),
                    "exp" to 0
                )
            )
        )

        val client = vertx.createHttpClient()
        val request = client
            .request(HttpMethod.POST, 9090, "localhost", "/api/collect/${Project.DEMO_PROJECT_ID}")
            .await()

        val response = request
            .putHeader("Authorization", "Bearer $token")
            .send("{}")
            .await()

        Assert.assertEquals(404, response.statusCode())
    }

    @RepeatedTest(3)
    fun ingest_json_ld(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
        val databaseUrn = "urn:tr:::postgres/example.com/${Random().nextLong()}"
        val tableUrn = "urn:tr:::postgres/example.com/${Random().nextLong()}/${Random().nextLong()}"

        val response = sendRequest(
            vertx,
            jacksonObjectMapper().writeValueAsString(
                mapOf(
                    "@context" to "http://trawler.dev/schema/core",
                    "@graph" to listOf(
                        mapOf(
                            "@type" to "tr:SqlDatabase",
                            "@id" to databaseUrn,
                            "name" to "foo",
                            "tr:has" to listOf(
                                mapOf("@type" to "tr:SqlTable", "@id" to tableUrn, "name" to "foo")
                            )
                        )
                    )
                )
            )
        )

        Assert.assertEquals(200, response.statusCode())

        val buffer = response.body().await()
        Assert.assertNotNull(buffer)

        val databaseFacetLog = newSuspendedTransaction {
            FacetLog.select { FacetLog.entityUrn.eq(databaseUrn) }.toList()
        }

        Assert.assertEquals(3, databaseFacetLog.size)

        val tableFacetLog = newSuspendedTransaction {
            FacetLog.select { FacetLog.entityUrn.eq(tableUrn) }.toList()
        }

        Assert.assertEquals(2, tableFacetLog.size)
    }
}
