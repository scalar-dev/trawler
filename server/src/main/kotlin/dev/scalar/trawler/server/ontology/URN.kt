package dev.scalar.trawler.server.ontology

data class FacetConstructor(
    val facet: Facet,
    val mapping: Any
)

data class Schema(
    val name: String,
    val formatStr: String,
    val facetConstructors: Set<FacetConstructor>
)
data class URN(
    val formatStr: String,
    val parameters: Map<String, Facet>
)
