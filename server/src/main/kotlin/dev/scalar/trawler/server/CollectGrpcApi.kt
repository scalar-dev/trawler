package dev.scalar.trawler.server

import io.vertx.core.AbstractVerticle
import io.vertx.grpc.VertxServer
import io.vertx.grpc.VertxServerBuilder
import org.apache.logging.log4j.LogManager

class CollectGrpcApi : AbstractVerticle() {
    val log = LogManager.getLogger()

    override fun start() {
        val rpcServer: VertxServer = VertxServerBuilder
            .forAddress(vertx, "localhost", 9090)
            .addService(Trawler())
            .build()

        log.info("Starting collector")
        rpcServer.start()
    }
}