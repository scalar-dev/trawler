package dev.scalar.trawler.server.verticle

import com.apicatalog.jsonld.JsonLd
import com.apicatalog.jsonld.document.JsonDocument
import com.apicatalog.jsonld.loader.DocumentLoader
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.readValue
import dev.scalar.trawler.ontology.config.OntologyConfig
import dev.scalar.trawler.server.auth.Users
import dev.scalar.trawler.server.auth.mintToken
import dev.scalar.trawler.server.collect.ApiKeyAuthProvider
import dev.scalar.trawler.server.collect.CollectRequest
import dev.scalar.trawler.server.collect.CollectResponse
import dev.scalar.trawler.server.collect.FacetStore
import dev.scalar.trawler.server.db.Account
import dev.scalar.trawler.server.db.AccountRole
import dev.scalar.trawler.server.db.ApiKey
import dev.scalar.trawler.server.ontology.OntologyCache
import dev.scalar.trawler.server.ontology.OntologyUpload
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.ext.auth.User
import io.vertx.ext.web.Router
import io.vertx.ext.web.common.WebEnvironment
import io.vertx.ext.web.handler.APIKeyHandler
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.dispatcher
import jakarta.json.JsonStructure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ontologyToContext
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID
import kotlin.system.measureTimeMillis

class CollectApi(val apiKeyAuthProvider: ApiKeyAuthProvider) : BaseVerticle() {
    val log = LogManager.getLogger()

    private suspend fun checkProjectAccess(user: User, projectId: UUID): Boolean = newSuspendedTransaction {
        ApiKey
            .innerJoin(Account)
            .innerJoin(AccountRole)
            .select {
                ApiKey.secret.eq(user.principal().getString("username")) and
                    AccountRole.projectId.eq(projectId) and
                        AccountRole.role.eq(AccountRole.ADMIN)
            }
            .count() == 1L
    }

    override suspend fun start() {
        super.start()
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
            .handler(BodyHandler.create())
            .handler(APIKeyHandler.create(apiKeyAuthProvider))

        if (WebEnvironment.development()) {
            log.info("Development token: ${mintToken(jwtAuth, Users.DEV, listOf("collect"))}")
        }

        router
            .route(HttpMethod.POST, "/api/ontology/:projectId")
            .handler { rc ->
                GlobalScope.launch(rc.vertx().dispatcher()) {
                    val projectId = UUID.fromString(rc.pathParam("projectId"))

                    if (!checkProjectAccess(rc.user(), projectId)) {
                        rc.fail(401)
                    } else {
                        val ontology = DatabindCodec.mapper().readValue<OntologyConfig>(rc.bodyAsString)
                        OntologyUpload(vertx).upload(projectId, ontology)
                        rc.response().send()
                    }
                }
            }

        router
            .route(HttpMethod.POST, "/api/collect/:projectId")
            .handler { rc ->
                GlobalScope.launch(rc.vertx().dispatcher()) {
                    val projectId = UUID.fromString(rc.pathParam("projectId"))

                    if (!checkProjectAccess(rc.user(), projectId)) {
                        rc.fail(401)
                    } else {
                        try {
                            val json = DatabindCodec.mapper().readValue<JsonStructure>(rc.bodyAsString)
                            val doc = JsonDocument.of(json)
                            val loader = DocumentLoader { url, _ ->
                                JsonDocument.of(ontologyToContext(ontologyCache.get(projectId)))
                            }
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
                }
            }

        vertx.createHttpServer().requestHandler(router).listen(9090)
    }
}
