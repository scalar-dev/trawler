package dev.scalar.trawler.ontology.config

data class OntologyConfig(
    val entityTypes: List<EntityTypeConfig>,
    val facetTypes: List<FacetTypeConfig>
)