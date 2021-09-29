package dev.scalar.trawler.server.db

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.scalar.trawler.server.db.util.jsonb
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.timestamp

object FacetValue : Table("facet_value") {
    val projectId = uuid("project_id").references(Project.id)
    val entityId = uuid("entity_id").references(Entity.id)
    val typeId = uuid("type_id").references(FacetType.id)
    val index = short("index")
    val value = jsonb("value", Any::class.java, jacksonObjectMapper()).nullable().default(null)
    val targetEntityId = uuid("target_entity_id").references(Entity.id).nullable()
    val entityTypeId = uuid("entity_type_id").references(EntityType.id).nullable()
    val version = long("version")
    val updatedAt = timestamp("updated_at")
}
