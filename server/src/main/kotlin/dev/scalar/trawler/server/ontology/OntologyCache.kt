package dev.scalar.trawler.server.ontology

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import dev.scalar.trawler.ontology.FacetMetaType
import dev.scalar.trawler.ontology.Ontology
import dev.scalar.trawler.ontology.OntologyImpl
import dev.scalar.trawler.server.db.EntityType
import dev.scalar.trawler.server.db.FacetType
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

inline fun <reified T : Enum<T>, V> ((T) -> V).find(value: V): T? {
    return enumValues<T>().firstOrNull { this(it) == value }
}

fun loadOntology(projectId: UUID?) = transaction {
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
                    facetTypeDb[FacetType.projectId]?.value
                )
            }
            .toSet(),
        emptySet()
    )
}

object OntologyCache {
    val log = LogManager.getLogger()
    val CACHE = CacheBuilder.newBuilder()
        .maximumSize(100)
        .build(
            object : CacheLoader<UUID, Ontology>() {
                override fun load(key: UUID): Ontology {
                    log.info("loading ontology for project $key")
                    return loadOntology(key)
                }
            }
        )
}
