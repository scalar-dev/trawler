package dev.scalar.trawler.ontology.config

import dev.scalar.trawler.ontology.FacetMetaType

data class FacetTypeConfig(
    val uri: String,
    val name: String,
    val metaType: FacetMetaType,
    val indexTimeSeries: Boolean = false,
    val jsonSchema: Any? = null
)