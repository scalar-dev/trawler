package dev.scalar.trawler.server

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.scalar.trawler.server.db.Account
import dev.scalar.trawler.server.db.ApiKey
import dev.scalar.trawler.server.db.FacetLog
import dev.scalar.trawler.server.db.Project
import dev.scalar.trawler.server.util.BaseVerticleTest
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpMethod
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
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.Random
import java.util.UUID

@Testcontainers
@ExtendWith(VertxExtension::class)
class CollectEndpointShould : BaseVerticleTest() {
    @BeforeEach
    fun setUp(vertx: Vertx, testContext: VertxTestContext) {
        deployCollectApi(vertx, testContext)
    }

    private suspend fun sendRequest(vertx: Vertx, body: String): HttpClientResponse {
        val client = vertx.createHttpClient()
        val request = client
            .request(HttpMethod.POST, 9090, "localhost", "/api/collect/${Project.DEMO_PROJECT_ID}")
            .await()

        return request
            .putHeader("X-API-Key", key)
            .send(body)
            .await()
    }

    @Test
    fun reject_bad_key(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
        val client = vertx.createHttpClient()
        val request = client
            .request(HttpMethod.POST, 9090, "localhost", "/api/collect/${Project.DEMO_PROJECT_ID}")
            .await()

        val response = request
            .putHeader("X-API-Key", "foo")
            .send("{}")
            .await()

        Assert.assertEquals(401, response.statusCode())
    }

    @Test
    fun reject_no_key(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
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
        newSuspendedTransaction {
            Account.insertIgnore {
                it[Account.id] = UUID(0, 2)
                it[Account.password] = "NO_LOGIN"
            }

            ApiKey.insertIgnore {
                it[ApiKey.secret] = apiKeyAuthProvider.hashKey("foo")
                it[ApiKey.accountId] = UUID(0, 2)
            }
        }

        val client = vertx.createHttpClient()
        val request = client
            .request(HttpMethod.POST, 9090, "localhost", "/api/collect/${Project.DEMO_PROJECT_ID}")
            .await()

        val response = request
            .putHeader("X-API-Key", "foo")
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
