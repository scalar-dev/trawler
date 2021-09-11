package dev.scalar.trawler.server

import com.apicatalog.jsonld.JsonLd
import com.apicatalog.jsonld.document.JsonDocument
import com.apicatalog.jsonld.loader.DocumentLoader
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.readValue
import dev.scalar.trawler.server.collect.CollectRequest
import dev.scalar.trawler.server.collect.FacetStore
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.common.WebEnvironment
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.JWTAuthHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import jakarta.json.JsonStructure
import kotlinx.coroutines.launch
import org.apache.logging.log4j.LogManager
import java.io.FileInputStream
import java.util.*

class CollectJsonApi : CoroutineVerticle() {
    val log = LogManager.getLogger()
    val facetStore = FacetStore()

    override suspend fun start() {
        val router = Router.router(vertx)
        router.route().handler(BodyHandler.create())

        val provider: JWTAuth = JWTAuth.create(
            vertx, JWTAuthOptions()
                .addPubSecKey(
                    PubSecKeyOptions()
                        .setAlgorithm("HS256")
                        .setBuffer("keyboard cat")
                )
        )

        if (WebEnvironment.development()) {
            val devToken = provider.generateToken(
                JsonObject(
                    mapOf(
                        "sub" to UUID.randomUUID().toString()
                    )
                )
            )
            log.info("Development token: $devToken")
        }

        val loader = DocumentLoader { url, options ->
            if (url.toString() == "http://trawler.dev/schema/core/0.1") {
                JsonDocument.of(FileInputStream("./core.jsonld"))
            } else {
                throw Exception()
            }
        }
        router
            .errorHandler(400) { rc ->
                rc.json(
                    mapOf("message" to rc.failure().message)
                )
                rc.fail(403)
            }
            .route(HttpMethod.POST, "/api/collect/:projectId")
            .handler(JWTAuthHandler.create(provider))
            .handler { rc ->
                launch(rc.vertx().dispatcher()) {
                    try {
                        val projectId = UUID.fromString(rc.pathParam("projectId"))
                        val user = rc.user()

                        try {
                            val json = DatabindCodec.mapper().readValue<JsonStructure>(rc.bodyAsString)
                            val doc = JsonDocument.of(json)
                            val flat = JsonLd.flatten(doc).loader(loader).get()

                            val request = DatabindCodec.mapper().convertValue<CollectRequest>(flat)
                            facetStore.ingest(projectId, request)

                            rc.response().send(DatabindCodec.mapper().writeValueAsString("cool"))
                        } catch (e: IllegalArgumentException) {
                            log.error("Exception processing collect", e)
                            rc.fail(400, e)
                        }
                    } catch (e: Exception) {
                        log.error("Exception processing collect", e)
                        rc.fail(e)
                    }
                }
            }

        vertx.createHttpServer().requestHandler(router).listen(9090)
    }
}