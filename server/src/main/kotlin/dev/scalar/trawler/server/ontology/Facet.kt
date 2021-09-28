package dev.scalar.trawler.server.ontology

import dev.scalar.trawler.server.db.FacetType
import io.vertx.json.schema.Schema

data class Facet(
    val uri: String,
    val name: String,
    val metaType: FacetType.MetaType,
    val jsonSchema: Schema?,
    val searchable: Boolean,
    val timeSeries: Boolean
)