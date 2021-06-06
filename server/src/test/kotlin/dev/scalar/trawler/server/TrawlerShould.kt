package dev.scalar.trawler.server

import dev.scalar.yak.RecordRequest
import dev.scalar.yak.TrawlerGrpcKt
import io.grpc.ManagedChannel
import io.vertx.core.Vertx
import io.vertx.grpc.VertxChannelBuilder
import io.vertx.junit5.VertxExtension
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit


@ExtendWith(VertxExtension::class)
class TrawlerShould {
    @Test
    @Timeout(5, unit = TimeUnit.SECONDS)
    fun respond(vertx: Vertx) = runBlocking(vertx.dispatcher()) {
        vertx.deployVerticle(App()).await()
        val channel: ManagedChannel = VertxChannelBuilder
            .forAddress(vertx, "localhost", 9090)
            .usePlaintext()
            .build()
        val stub = TrawlerGrpcKt.TrawlerCoroutineStub(channel)

        val response = stub.record(RecordRequest.getDefaultInstance())
    }
}