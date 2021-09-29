package dev.scalar.trawler.server

import com.apicatalog.jsonld.JsonLd
import com.apicatalog.jsonld.document.JsonDocument
import com.apicatalog.jsonld.loader.DocumentLoader
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.readValue
import dev.scalar.trawler.ontology.config.OntologyConfig
import dev.scalar.trawler.server.collect.CollectRequest
import dev.scalar.trawler.server.collect.CollectResponse
import dev.scalar.trawler.server.collect.FacetStore
import dev.scalar.trawler.server.ontology.OntologyCache
import dev.scalar.trawler.server.ontology.OntologyUpload
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.LogManager
import java.io.FileInputStream
import java.util.*
import kotlin.system.measureTimeMillis


class CollectJsonApi : CoroutineVerticle() {
    val log = LogManager.getLogger()

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
            if (url.toString() == "http://trawler.dev/schema/core") {
                JsonDocument.of(FileInputStream("./core.jsonld"))
            } else {
                throw Exception()
            }
        }

        router
            .errorHandler(500) { rc ->
                rc.json(
                    mapOf("message" to rc.failure().message)
                )
                rc.fail(403)
            }
            .route(HttpMethod.POST, "/api/ontology/:projectId")
            .handler(JWTAuthHandler.create(provider))
            .handler { rc ->
                GlobalScope.launch(rc.vertx().dispatcher()) {
                    val projectId = UUID.fromString(rc.pathParam("projectId"))
                    val ontology = DatabindCodec.mapper().readValue<OntologyConfig>(rc.bodyAsString)

                    OntologyUpload().upload(projectId, ontology)

                    rc.response().send()
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
                GlobalScope.launch(rc.vertx().dispatcher()) {
                    try {
                        val projectId = UUID.fromString(rc.pathParam("projectId"))
                        val user = rc.user()

                        try {
                            val json = DatabindCodec.mapper().readValue<JsonStructure>(rc.bodyAsString)
                            val doc = JsonDocument.of(json)
                            val flat = JsonLd.flatten(doc).loader(loader).get()

                            val request = DatabindCodec.mapper().convertValue<CollectRequest>(flat)

                            val time = measureTimeMillis {
                                val storeResult = withContext(Dispatchers.IO) {
                                    val facetStore = FacetStore(OntologyCache.CACHE[projectId])
                                    val result = facetStore.ingest(projectId, request)
                                    result.ids.forEach { vertx.eventBus().send("indexer.queue", it.toString()) }
                                    result
                                }

                                rc.response().send(DatabindCodec.mapper().writeValueAsString(
                                    CollectResponse(
                                        storeResult.txId,
                                        storeResult.unrecognisedFacetTypes,
                                        storeResult.unrecognisedEntityTypes
                                    )
                                ))
                            }
                            log.info("took ${time}ms")
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