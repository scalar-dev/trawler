package dev.scalar.trawler.server.graphql

import dev.scalar.trawler.server.db.EntityType
import dev.scalar.trawler.server.db.FacetType
import dev.scalar.trawler.server.db.FacetValue
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class EntityQuery {
    private fun fetchEntities(ids: List<UUID>): List<dev.scalar.trawler.server.graphql.Entity> =
        transaction {
            FacetValue
                .join(FacetType, JoinType.INNER, FacetType.id, FacetValue.typeId)
                .join(dev.scalar.trawler.server.db.Entity, JoinType.INNER, dev.scalar.trawler.server.db.Entity.id, FacetValue.entityId)
                .join(dev.scalar.trawler.server.db.EntityType, JoinType.LEFT, FacetValue.entityTypeId, dev.scalar.trawler.server.db.EntityType.id)
                .select {
                    FacetValue.entityId.inList(ids)
                }
                .groupBy { it[FacetValue.entityId] }
                .map {
                    val type = it.value.map { it[dev.scalar.trawler.server.db.EntityType.uri] }
                        .filterNotNull()
                        .first()

                    val facetsByUri = it.value.groupBy { it[FacetType.uri] }

                    val relationships =
                        it.value
                            .filter { it[FacetType.metaType] == FacetType.MetaType.RELATIONSHIP.value }
                            .groupBy { it[FacetType.uri] }
                            .map {
                                val name = it.value.first()[FacetType.name]
                                val version = it.value.first()[FacetValue.version]
                                val metaType = it.value.first()[FacetType.metaType]

                                Facet(
                                    it.key,
                                    name,
                                    metaType,
                                    version,
                                    it.value.map { it[FacetValue.targetEntityId] }
                                )
                            }

                    val others = it.value
                        .filter {
                            it[FacetType.metaType] != FacetType.MetaType.RELATIONSHIP.value &&
                                    it[EntityType.uri] == null
                        }
                        .groupBy { it[FacetType.uri] }
                        .map {
                            val name = it.value.first()[FacetType.name]
                            val version = it.value.first()[FacetValue.version]
                            val metaType = it.value.first()[FacetType.metaType]

                            Facet(
                                it.key,
                                name,
                                metaType,
                                version,
                                it.value.map { it[FacetValue.value] }
                            )
                        }
                    Entity(
                        it.key,
                        type,
                        relationships + others
                    )
                }
        }

    fun entityById(id: UUID, d: Int): List<dev.scalar.trawler.server.graphql.Entity> {
        var currentEntities = fetchEntities(listOf(id))
        val output = currentEntities.toMutableList()

        for (i in 0 until d) {
            val targetIds = currentEntities.flatMap { entity ->
                entity.facets.filter {
                    it.metaType == FacetType.MetaType.RELATIONSHIP.value
                }
                .flatMap { (it.value as List<UUID>) }
            }

            val fromIds = transaction {
                FacetValue
                    .join(dev.scalar.trawler.server.db.Entity, JoinType.INNER, dev.scalar.trawler.server.db.Entity.id, FacetValue.entityId)
                    .slice(dev.scalar.trawler.server.db.Entity.id)
                    .select { FacetValue.targetEntityId.inList(currentEntities.map { it.entityId }) }
                    .map { it[dev.scalar.trawler.server.db.Entity.id].value }
            }

            val existingIds = output.map { it.entityId }.toSet()

            val idsToFetch = (targetIds + fromIds).filter { !existingIds.contains(it) }

            currentEntities = fetchEntities(idsToFetch)
            output.addAll(currentEntities)
        }

        return output
    }
}