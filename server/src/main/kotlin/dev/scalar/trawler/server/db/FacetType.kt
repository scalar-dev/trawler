package dev.scalar.trawler.server.db

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.scalar.trawler.server.db.util.jsonb
import org.jetbrains.exposed.dao.id.UUIDTable

object FacetType : UUIDTable("facet_type") {
    val projectId = reference("project_id", Project.id).nullable()
    val name = text("name")
    val uri = text("uri")
    val metaType = text("meta_type")
    val isDeprecated = bool("is_deprecated")
    val indexTimeSeries = bool("index_time_series")
    val jsonSchema = jsonb("json_schema", Any::class.java, jacksonObjectMapper()).nullable()
}
