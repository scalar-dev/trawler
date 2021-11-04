package dev.scalar.trawler.server

import dev.scalar.trawler.server.db.ApiKey
import dev.scalar.trawler.server.util.BaseVerticleTest
import io.vertx.core.Vertx
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class ApiKeysShould : BaseVerticleTest() {
    @BeforeEach
    fun setUp(vertx: Vertx, testContext: VertxTestContext) {
        initialSetup(vertx)
        deployGraphQL(vertx, testContext)
    }

    @Test
    fun create_api_key(vertx: Vertx) = runBlocking(vertx.dispatcher()) {
        val response = graphQLRequest(
            vertx,
            """
                mutation CreateKey {
                    createApiKey {
                        id
                    }
                }
            """.trimIndent()
        )

        val createApiKey = response.data!!["createApiKey"] as Map<*, *>
        val id = UUID.fromString(createApiKey["id"] as String)

        val apiKey = newSuspendedTransaction {
            ApiKey.select { ApiKey.id.eq(id) }.firstOrNull()
        }
        Assertions.assertNotNull(apiKey)
    }
}
