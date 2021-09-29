package dev.scalar.trawler.ontology

import java.util.*

data class ParsedUrn(
    val namespace: String,
    val projectId: UUID?,
    val segment: Map<String, String>,
    val urnSpace: String,
    val urnFacets: Map<String, Any>
)