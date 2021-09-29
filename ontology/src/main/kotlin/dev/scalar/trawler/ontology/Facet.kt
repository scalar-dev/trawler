package dev.scalar.trawler.ontology

import io.vertx.json.schema.Schema

data class Facet(
    val uri: String,
    val name: String,
    val metaType: FacetMetaType,
    val jsonSchema: Schema?,
    val searchable: Boolean,
    val timeSeries: Boolean
)