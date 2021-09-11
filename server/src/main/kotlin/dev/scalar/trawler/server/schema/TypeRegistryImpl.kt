package dev.scalar.trawler.server.schema

import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

class TypeRegistryImpl : TypeRegistry {
    val entityTypes = mutableMapOf<String, EntityType?>()
    val facetTypes = mutableMapOf<String, FacetType?>()

    override suspend fun entityTypeByUri(uri: String): EntityType? = entityTypes.getOrPut(uri) {
        newSuspendedTransaction {
            val entityTypeDb = dev.scalar.trawler.server.db.EntityType
                .select { dev.scalar.trawler.server.db.EntityType.uri eq uri }
                .firstOrNull()

            if (entityTypeDb == null) {
                null
            } else {
                EntityType(
                    uri,
                    entityTypeDb[dev.scalar.trawler.server.db.EntityType.id].value,
                    entityTypeDb[dev.scalar.trawler.server.db.EntityType.name],
                    emptySet()
                )
            }
        }
    }

    inline fun <reified T : Enum<T>, V> ((T) -> V).find(value: V): T? {
        return enumValues<T>().firstOrNull { this(it) == value }
    }

    override suspend fun facetTypeByUri(uri: String): FacetType? = facetTypes.getOrPut(uri) {
        newSuspendedTransaction {
            val facetTypeDb = dev.scalar.trawler.server.db.FacetType
                .select { dev.scalar.trawler.server.db.FacetType.uri eq uri }
                .firstOrNull()

            if (facetTypeDb == null) {
                null
            } else {
                FacetType(
                    uri,
                    facetTypeDb[dev.scalar.trawler.server.db.FacetType.id].value,
                    dev.scalar.trawler.server.db.FacetType.MetaType::value.find(facetTypeDb[dev.scalar.trawler.server.db.FacetType.metaType])!!,
                )
            }
        }
    }

}