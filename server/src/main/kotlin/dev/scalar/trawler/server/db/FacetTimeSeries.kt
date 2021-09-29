package dev.scalar.trawler.server.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.timestamp

object FacetTimeSeries : Table("facet_time_series") {
    val entityId = uuid("entity_id").references(Entity.id)
    val typeId = uuid("type_id").references(FacetType.id)
    val timestamp = timestamp("timestamp")
    val valueDouble = double("value_double")
    val valueLong = long("value_long")
    val version = long("version")
}
