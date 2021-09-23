package dev.scalar.trawler.server.ontology

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

inline fun <reified T : Enum<T>, V> ((T) -> V).find(value: V): T? {
    return enumValues<T>().firstOrNull { this(it) == value }
}

object OntologyCache {
    val log = LogManager.getLogger()
    val CACHE = CacheBuilder.newBuilder()
        .maximumSize(100)
        .build(
            object: CacheLoader<UUID, Ontology>(){
                override fun load(key: UUID): Ontology {
                    log.info("loading ontology for project $key")
                    return transaction {
                        OntologyImpl(
                            dev.scalar.trawler.server.db.EntityType
                                .select {
                                    dev.scalar.trawler.server.db.EntityType.projectId eq key or
                                            dev.scalar.trawler.server.db.EntityType.projectId.isNull()
                                }
                                .map { entityTypeDb ->
                                    EntityType(
                                        entityTypeDb[dev.scalar.trawler.server.db.EntityType.uri],
                                        entityTypeDb[dev.scalar.trawler.server.db.EntityType.id].value,
                                        entityTypeDb[dev.scalar.trawler.server.db.EntityType.name],
                                        emptySet()
                                    )
                                }
                                .toSet(),
                            dev.scalar.trawler.server.db.FacetType
                                .select {
                                    dev.scalar.trawler.server.db.FacetType.projectId eq key or
                                            dev.scalar.trawler.server.db.FacetType.projectId.isNull()
                                }
                                .map { facetTypeDb ->
                                    FacetType(
                                        facetTypeDb[dev.scalar.trawler.server.db.FacetType.uri],
                                        facetTypeDb[dev.scalar.trawler.server.db.FacetType.id].value,
                                        dev.scalar.trawler.server.db.FacetType.MetaType::value.find(facetTypeDb[dev.scalar.trawler.server.db.FacetType.metaType])!!,
                                    )
                                }
                                .toSet(),
                            emptySet()
                        )
                    }
                }
            }
        )

}