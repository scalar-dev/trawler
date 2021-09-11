package dev.scalar.trawler.server.db

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.timestamp

object FacetLog : Table("facet_log") {
    val projectId = reference("project_id", Project.id)
    val entityId = reference("entity_id", Entity.id)
    val typeId = reference("type_id", FacetType.id)
    val version = long("version")
    val index = short("index")
    val timestamp = timestamp("timestamp").nullable()
    val value = jsonb("value", Any::class.java, jacksonObjectMapper()).nullable()
    val targetEntityId = reference("target_entity_id", Entity.id)
}