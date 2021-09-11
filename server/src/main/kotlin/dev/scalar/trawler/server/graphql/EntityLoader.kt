package dev.scalar.trawler.server.graphql

import dev.scalar.trawler.server.db.EntityType
import dev.scalar.trawler.server.db.FacetLog
import dev.scalar.trawler.server.db.FacetType
import dev.scalar.trawler.server.db.customDistinctOn
import org.dataloader.BatchLoader
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

class EntityLoader : BatchLoader<UUID, Entity> {
    override fun load(keys: List<UUID>): CompletionStage<List<Entity>> {
        val facets = transaction {
            FacetLog
                .join(FacetType, JoinType.LEFT, FacetType.id, FacetLog.typeId)
                .slice(
                    customDistinctOn(FacetLog.entityId, FacetLog.typeId, FacetLog.index),
                    FacetLog.entityId,
                    FacetType.uri,
                    FacetType.metaType,
                    FacetLog.value,
                    FacetLog.version,
                    FacetLog.targetEntityId
                )
                .select { FacetLog.entityId.inList(keys) }
                .orderBy(
                    FacetLog.entityId to SortOrder.DESC,
                    FacetLog.typeId to SortOrder.DESC,
                    FacetLog.index to SortOrder.DESC,
                    FacetLog.version to SortOrder.DESC
                )
                .map {
                    Facet(
                        it[FacetLog.entityId].value,
                        it[FacetType.uri],
                        it[FacetLog.version],
                        it[FacetLog.value]?.toString() ?: it[FacetLog.targetEntityId].value.toString(),
                        it[FacetType.metaType]
                    )
                }
        }

        val types = transaction {
            dev.scalar.trawler.server.db.Entity
                .join(EntityType, JoinType.LEFT, EntityType.id, dev.scalar.trawler.server.db.Entity.typeId)
                .select { dev.scalar.trawler.server.db.Entity.id.inList(keys) }
                .associate { it[dev.scalar.trawler.server.db.Entity.id].value to it[EntityType.uri] }
        }

        return CompletableFuture.completedStage(keys.map { entityId ->
            val facetsByUri = facets
                .filter { it.entityId == entityId }
                .groupBy { it.uri }
                .flatMap {
                    val version = it.value.maxOf { facet -> facet.version }
                    it.value.filter { facet -> facet.version == version }
                }

            Entity(
                entityId,
                types[entityId]!!,
                facetsByUri
            )
        })
    }
}