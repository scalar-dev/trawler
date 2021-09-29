package dev.scalar.trawler.server

import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.junit5.VertxExtension
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit

@ExtendWith(VertxExtension::class)
class HelloWorldEndpointShould {
    @Test
    @Timeout(5, unit = TimeUnit.SECONDS)
    fun respond_to_query(vertx: Vertx) = runBlocking(vertx.dispatcher()) {
        vertx.deployVerticle(App()).await()
        val client = vertx.createHttpClient()
        val request = client.request(HttpMethod.GET, 8080, "localhost", "/hello").await()
        val response = request.send().await()
        val buffer = response.body().await()
        assertEquals(buffer.toString(), "HELLO")
    }
}
