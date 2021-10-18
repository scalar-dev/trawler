package dev.scalar.trawler.server.ontology

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import dev.scalar.trawler.ontology.FacetMetaType
import dev.scalar.trawler.ontology.Ontology
import dev.scalar.trawler.ontology.OntologyImpl
import dev.scalar.trawler.server.db.EntityType
import dev.scalar.trawler.server.db.FacetType
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.json.schema.Schema
import io.vertx.json.schema.SchemaParser
import io.vertx.json.schema.SchemaRouter
import io.vertx.json.schema.SchemaRouterOptions
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

inline fun <reified T : Enum<T>, V> ((T) -> V).find(value: V): T? {
    return enumValues<T>().firstOrNull { this(it) == value }
}

fun loadJsonSchema(jsonSchema: Any?): Schema? {
    if (jsonSchema == null) {
        return null
    }

    val schemaRouter: SchemaRouter = SchemaRouter.create(Vertx.vertx(), SchemaRouterOptions())
    val schemaParser: SchemaParser = SchemaParser.createDraft201909SchemaParser(schemaRouter)
    return schemaParser.parse(JsonObject.mapFrom(jsonSchema))
}

fun loadOntology(vertx: Vertx, projectId: UUID?) = transaction {
    OntologyImpl(
        EntityType
            .select {
                EntityType.projectId eq projectId or
                    EntityType.projectId.isNull()
            }
            .map { entityTypeDb ->
                dev.scalar.trawler.ontology.EntityType(
                    entityTypeDb[EntityType.uri],
                    entityTypeDb[EntityType.id].value,
                    entityTypeDb[EntityType.name],
                    emptySet(),
                    entityTypeDb[EntityType.projectId]?.value
                )
            }
            .toSet(),
        FacetType
            .select {
                FacetType.projectId eq projectId or
                    FacetType.projectId.isNull()
            }
            .map { facetTypeDb ->
                dev.scalar.trawler.ontology.FacetType(
                    facetTypeDb[FacetType.uri],
                    facetTypeDb[FacetType.id].value,
                    facetTypeDb[FacetType.name],
                    FacetMetaType::value.find(facetTypeDb[FacetType.metaType])!!,
                    facetTypeDb[FacetType.projectId]?.value,
                    facetTypeDb[FacetType.indexTimeSeries],
                    loadJsonSchema(facetTypeDb[FacetType.jsonSchema])
                )
            }
            .toSet(),
        emptySet()
    )
}

class OntologyCache(val vertx: Vertx) {
    val log = LogManager.getLogger()
    private val cache = CacheBuilder.newBuilder()
        .maximumSize(100)
        .build(
            object : CacheLoader<UUID, Ontology>() {
                override fun load(key: UUID): Ontology {
                    log.info("loading ontology for project $key")
                    return loadOntology(vertx, key)
                }
            }
        )

    fun get(projectId: UUID): Ontology = cache.get(projectId)
}
