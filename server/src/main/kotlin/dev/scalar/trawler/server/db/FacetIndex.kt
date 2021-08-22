package dev.scalar.trawler.server.db

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.timestamp

object FacetIndex : UUIDTable("facet_index") {
    val facetId = reference("facet_id", Facet.id)
    val version = long("version")
    val index = long("index")
    val timestap = timestamp("timestamp").nullable()
    val valueString = text("value_string").nullable()
    val valueDouble = double("value_double").nullable()
    val valueInt = long("value_int").nullable()
    val valueEntityId = reference("value_entity_id", Entity.id)
    val valueTargetEntityId = reference("value_target_entity_id", Entity.id)
}