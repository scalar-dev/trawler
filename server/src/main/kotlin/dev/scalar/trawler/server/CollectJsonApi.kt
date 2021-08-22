package dev.scalar.trawler.server

import com.apicatalog.jsonld.JsonLd
import com.apicatalog.jsonld.document.JsonDocument
import com.apicatalog.jsonld.loader.DocumentLoader
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.readValue
import dev.scalar.trawler.server.db.*
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
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.io.FileInputStream
import java.util.*
import kotlin.system.measureTimeMillis

class CollectJsonApi : CoroutineVerticle() {
    val log = LogManager.getLogger()
    val entityTypeIdMap = mutableMapOf<String, UUID?>()
    val facetTypeIdMap = mutableMapOf<String, UUID?>()

    private suspend fun lookupEntityTypeId(uri: String): UUID? = entityTypeIdMap.getOrPut(uri) {
        newSuspendedTransaction {
            EntityType
                .slice(EntityType.id)
                .select { EntityType.uri eq uri }
                .firstOrNull()?.get(EntityType.id)?.value
        }
    }

    private suspend fun lookupFacetTypeId(uri: String): UUID? = facetTypeIdMap.getOrPut(uri) {
        newSuspendedTransaction {
            FacetType
                .slice(FacetType.id)
                .select { FacetType.uri eq uri }
                .firstOrNull()?.get(FacetType.id)?.value
        }
    }

    fun urnToTypeId(urn: String): UUID {
        val type = urn.split(":")[2]

        if (type == "table") {
            return UUID.fromString("76ef5961-b588-4988-923c-883909756a0a")
        } else if (type == "field") {
            return UUID.fromString("c3b321b7-3c98-4b76-9117-4c8f2a408c93")
        }

        throw java.lang.IllegalArgumentException("whatever")
    }

    suspend fun ingestFacet(projectId: UUID, snapshot: FacetSnapshot) {
        // Upsert the facet on the entity
        val facetWithType = newSuspendedTransaction {
            Facet.insertIgnore {
                it[Facet.entityId] = snapshot.entityId
                it[Facet.typeId] = snapshot.facetTypeId
                it[Facet.latestVersion] = 0
            }

            Facet
                .leftJoin(FacetType)
                .slice(Facet.id, FacetType.id, FacetType.metaType)
                .select { Facet.entityId.eq(snapshot.entityId) and Facet.typeId.eq(snapshot.facetTypeId) }
                .first()
        }

        val facetId = facetWithType[Facet.id].value
        val facetMetaType = facetWithType[FacetType.metaType]

        val version = newSuspendedTransaction {
            val idSnapshotValues = snapshot.values.filterIsInstance<FacetSnapshotValue.Id>()

            // Create a ghost entity if we need to
            Entity.batchInsert(idSnapshotValues, ignore = true) { value ->
                val typeId = urnToTypeId(value.value)

                // Create a ghost entity if we need to
                this[Entity.projectId] = projectId
                this[Entity.urn] = value.value
                this[Entity.typeId] = typeId
            }

            val entityIds = Entity
                .slice(Entity.id, Entity.urn)
                .select { Entity.urn inList idSnapshotValues.map { it.value } }
                .associate { it[Entity.urn] to it[Entity.id].value }

            val indexedValues = snapshot.values.mapIndexed { index, facetSnapshotValue ->  index to facetSnapshotValue }

            // Write to the log table and increment the version
            val facet = Facet.selectForUpdate {
                Facet.typeId.eq(snapshot.facetTypeId) and Facet.entityId.eq(snapshot.entityId)
            }.first()

            // increment version
            val version = facet[Facet.latestVersion] + 1

            // Need to check types here
            FacetLog.batchInsert(indexedValues) { indexValue ->
                val index = indexValue.first
                val value = indexValue.second

                this[FacetLog.facetId] = facetId
                this[FacetLog.version] = version
                this[FacetLog.index] = index.toLong()
                this[FacetLog.valueEntityId] = snapshot.entityId

                when (value) {
                    is FacetSnapshotValue.Id -> {
                        assert(facetMetaType == FacetType.MetaType.RELATIONSHIP.value || facetMetaType == FacetType.MetaType.RELATIONSHIP_OWNED.value)
                        this[FacetLog.valueTargetEntityId] = entityIds[value.value]
                    }
                    is FacetSnapshotValue.String -> {
                        assert(facetMetaType == FacetType.MetaType.STRING.value)
                        this[FacetLog.valueString] = value.value
                    }
                }
            }

            Facet.update({ Facet.id.eq(facetId) }) {
                it[Facet.latestVersion] = version
            }

            version
        }

        // Write to the index table
//        newSuspendedTransaction {
//            // remove any older stuff in the index
//            FacetIndex.deleteWhere { FacetIndex.facetId eq facetId and FacetIndex.version.lessEq(version) }
//
//            // We will fail here if someone else snuck in before us
//            FacetIndex.batchInsert(indexedValues) { indexValue ->
//                val index = indexValue.first
//                val value = indexValue.second
//
//                this[FacetIndex.facetId] = facetId
//                this[FacetIndex.version] = version
//                this[FacetIndex.index] = index.toLong()
//                this[FacetIndex.valueEntityId] = snapshot.entityId
//
//                when (value) {
//                    is FacetSnapshotValue.Id -> {
//                        assert(
//                            facetMetaType == FacetType.MetaType.RELATIONSHIP.value ||
//                                    facetMetaType == FacetType.MetaType.RELATIONSHIP_OWNED.value
//                        )
//
//                        this[FacetIndex.valueTargetEntityId] = entityIds[value.value]
//                    }
//                    is FacetSnapshotValue.String -> {
//                        assert(facetMetaType == FacetType.MetaType.STRING.value)
//                        this[FacetIndex.valueString] = value.value
//                    }
//                }
//            }
//        }

//        if (facetMetaType == FacetType.MetaType.RELATIONSHIP_OWNED.value) {
//            // CASCADE deletes
//        }
    }


    private suspend fun ingest(projectId: UUID, request: CollectRequest): CollectResponse {
        var facetCounter = 0
        var entityCounter = 0

        val time = measureTimeMillis {
            val entityTypes = request.nodes.associate { node ->
                node.id to lookupEntityTypeId(node.type.first())
            }

            val validNodes = request.nodes.filter { node -> entityTypes[node.id] != null }

            val entityIds = newSuspendedTransaction {
                Entity.batchInsert(validNodes, ignore = true, shouldReturnGeneratedValues = true) { node ->
                    this[Entity.projectId] = projectId
                    this[Entity.urn] = node.id
                    this[Entity.typeId] = entityTypes[node.id]
                }

                Entity
                    .slice(Entity.urn, Entity.id)
                    .select { Entity.urn inList validNodes.map { node -> node.id } }
                    .associate { it[Entity.urn] to it[Entity.id].value }
            }

            validNodes.forEach { node ->
                entityCounter += 1
                val entityId = entityIds[node.id]!!
                log.debug("ingesting $entityId")

                node.facets().forEach { keyValue ->
                    val facetTypeId = lookupFacetTypeId(keyValue.key)

                    if (facetTypeId == null) {
                        log.debug("Unrecognised facet ${keyValue.key}. Skipping")
                    } else {
                        facetCounter += 1
                        val facet = FacetSnapshot(
                            entityId,
                            facetTypeId,
                            keyValue.value.map { value ->
                                when {
                                    value.value != null -> {
                                        FacetSnapshotValue.String(value.value)
                                    }
                                    value.id != null -> {
                                        FacetSnapshotValue.Id(value.id)
                                    }
                                    else -> {
                                        throw IllegalArgumentException("Malformed collect facet: $value")
                                    }
                                }
                            }
                        )
                        ingestFacet(projectId, facet)
                    }
                }
            }
        }

        return CollectResponse(facetCounter, entityCounter, time)
    }

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
                            val response = ingest(projectId, request)

                            rc.response().send(DatabindCodec.mapper().writeValueAsString(response))
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