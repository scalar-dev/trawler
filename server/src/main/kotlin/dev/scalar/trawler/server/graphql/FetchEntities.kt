package dev.scalar.trawler.server.graphql

import dev.scalar.trawler.ontology.FacetMetaType
import dev.scalar.trawler.server.db.AccountRole
import dev.scalar.trawler.server.db.EntityType
import dev.scalar.trawler.server.db.FacetType
import dev.scalar.trawler.server.db.FacetValue
import dev.scalar.trawler.server.db.Project
import dev.scalar.trawler.server.graphql.type.Entity
import dev.scalar.trawler.server.graphql.type.Facet
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

suspend fun fetchEntities(accountId: UUID, ids: Collection<UUID>): List<Entity> =
    newSuspendedTransaction {
        FacetValue
            .join(FacetType, JoinType.INNER, FacetType.id, FacetValue.typeId)
            .join(dev.scalar.trawler.server.db.Entity, JoinType.INNER, dev.scalar.trawler.server.db.Entity.id, FacetValue.entityId)
            .join(Project, JoinType.INNER, dev.scalar.trawler.server.db.Project.id, dev.scalar.trawler.server.db.Entity.projectId)
            .join(AccountRole, JoinType.INNER, AccountRole.projectId, dev.scalar.trawler.server.db.Entity.projectId)
            .join(EntityType, JoinType.LEFT, FacetValue.entityTypeId, EntityType.id)
            .select {
                FacetValue.entityId.inList(ids) and AccountRole.accountId.eq(accountId)
            }
            .groupBy { it[FacetValue.entityId] }
            .map {
                val type = it.value.map { it[EntityType.uri] }
                    .filterNotNull()
                    .first()

                val typeName = it.value.map { it[EntityType.name] }
                    .filterNotNull()
                    .first()

                val relationships =
                    it.value
                        .filter { it[FacetType.metaType] == FacetMetaType.RELATIONSHIP.value }
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
                        it[FacetType.metaType] != FacetMetaType.RELATIONSHIP.value &&
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
                    it.value[0][dev.scalar.trawler.server.db.Entity.projectId].value,
                    it.value[0][dev.scalar.trawler.server.db.Entity.urn],
                    type,
                    typeName,
                    relationships + others
                )
            }
    }
