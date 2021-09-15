package dev.scalar.trawler.server.ontology

data class Ontology(
    val facets: Set<Facet>,
    val entities: Set<Entity>,

)