package dev.scalar.trawler.server.db

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.scalar.trawler.server.db.util.jsonb
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.timestamp

object FacetLog : UUIDTable("facet_log") {
    val txId = uuid("tx_id")
    val projectId = reference("project_id", Project.id)
    val entityUrn = text("entity_urn")
    val typeId = reference("type_id", FacetType.id)
    val version = long("version")
    val timestamp = timestamp("timestamp").nullable()
    val value = jsonb("value", List::class.java, jacksonObjectMapper()).nullable()
    val entityId = uuid("entity_id").references(Entity.id).nullable()
}
