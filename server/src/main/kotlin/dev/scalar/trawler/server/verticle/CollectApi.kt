package dev.scalar.trawler.server.verticle

import com.apicatalog.jsonld.JsonLd
import com.apicatalog.jsonld.document.JsonDocument
import com.apicatalog.jsonld.loader.DocumentLoader
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.readValue
import dev.scalar.trawler.ontology.config.OntologyConfig
import dev.scalar.trawler.server.App
import dev.scalar.trawler.server.auth.Users
import dev.scalar.trawler.server.auth.mintToken
import dev.scalar.trawler.server.collect.CollectRequest
import dev.scalar.trawler.server.collect.CollectResponse
import dev.scalar.trawler.server.collect.FacetStore
import dev.scalar.trawler.server.db.Account
import dev.scalar.trawler.server.db.AccountRole
import dev.scalar.trawler.server.ontology.OntologyCache
import dev.scalar.trawler.server.ontology.OntologyUpload
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.ext.web.Router
import io.vertx.ext.web.common.WebEnvironment
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.JWTAuthHandler
import io.vertx.kotlin.coroutines.dispatcher
import jakarta.json.JsonStructure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID
import kotlin.system.measureTimeMillis

class CollectApi : BaseVerticle() {
    val log = LogManager.getLogger()

    override suspend fun start() {
        super.start()
        configureDatabase()
        dbDefaults()

        val router = Router.router(vertx)
        val ontologyCache = OntologyCache(vertx)

        router
            .errorHandler(400) { rc ->
                rc.json(
                    mapOf("message" to rc.failure().message)
                )
                rc.fail(403)
            }
            .route()
            .handler(JWTAuthHandler.create(jwtAuth).withScope("collect"))
            .handler(BodyHandler.create())

        if (WebEnvironment.development()) {
            log.info("Development token: ${mintToken(jwtAuth, Users.DEV, listOf("collect"))}")
        }

        val loader = DocumentLoader { url, _ ->
            if (url.toString() == "http://trawler.dev/schema/core") {
                JsonDocument.of(App::class.java.getResourceAsStream("/core.jsonld"))
            } else {
                throw Exception()
            }
        }

        router
            .route(HttpMethod.POST, "/api/ontology/:projectId")
            .handler { rc ->
                GlobalScope.launch(rc.vertx().dispatcher()) {
                    val projectId = UUID.fromString(rc.pathParam("projectId"))
                    val ontology = DatabindCodec.mapper().readValue<OntologyConfig>(rc.bodyAsString)

                    OntologyUpload(vertx).upload(projectId, ontology)

                    rc.response().send()
                }
            }

        router
            .route(HttpMethod.POST, "/api/collect/:projectId")
            .handler { rc ->
                GlobalScope.launch(rc.vertx().dispatcher()) {
                    try {
                        val projectId = UUID.fromString(rc.pathParam("projectId"))
                        val user = rc.user()

                        val role = newSuspendedTransaction {
                            AccountRole
                                .innerJoin(Account)
                                .select {
                                    Account.id.eq(UUID.fromString(user.principal().getString("sub"))) and
                                        AccountRole.projectId.eq(projectId)
                                }
                                .map { it[AccountRole.role] }
                                .firstOrNull()
                        }

                        if (role == null) {
                            rc.fail(404)
                        } else {
                            try {
                                val json = DatabindCodec.mapper().readValue<JsonStructure>(rc.bodyAsString)
                                val doc = JsonDocument.of(json)
                                val flat = JsonLd.flatten(doc).loader(loader).get()

                                val request = DatabindCodec.mapper().convertValue<CollectRequest>(flat)

                                val time = measureTimeMillis {
                                    val storeResult = withContext(Dispatchers.IO) {
                                        val facetStore = FacetStore(ontologyCache.get(projectId))
                                        val result = facetStore.ingest(projectId, request)
                                        result.ids.forEach { vertx.eventBus().send("indexer.queue", it.toString()) }
                                        result
                                    }

                                    rc.response().send(
                                        DatabindCodec.mapper().writeValueAsString(
                                            CollectResponse(
                                                storeResult.txId,
                                                storeResult.unrecognisedFacetTypes,
                                                storeResult.unrecognisedEntityTypes
                                            )
                                        )
                                    )
                                }
                                log.info("took ${time}ms")
                            } catch (e: IllegalArgumentException) {
                                log.error("Exception processing collect", e)
                                rc.fail(400, e)
                            }
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
