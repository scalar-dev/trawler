package dev.scalar.trawler.server.graphql.query

import dev.scalar.trawler.server.graphql.QueryContext
import dev.scalar.trawler.server.graphql.Unauthenticated
import dev.scalar.trawler.server.graphql.type.EntityType
import dev.scalar.trawler.server.graphql.type.FacetType

class OntologyQuery {
    @Unauthenticated
    suspend fun facetTypes(context: QueryContext, project: String): List<FacetType> {
        val projectId = context.projectId(project)
        val ontology = context.ontologyCache.get(projectId)

        return ontology.facetTypes()
            .map {
                FacetType(
                    it.id,
                    it.uri,
                    it.name,
                    it.metaType.value,
                    it.projectId == null,
                    it.indexTimeSeries,
                    it.jsonSchema as Any?
                )
            }
    }

    @Unauthenticated
    suspend fun entityTypes(context: QueryContext, project: String): List<EntityType> {
        val projectId = context.projectId(project)
        val ontology = context.ontologyCache.get(projectId)

        return ontology.entityTypes()
            .map {
                EntityType(
                    it.id,
                    it.uri,
                    it.name,
                    it.projectId == null
                )
            }
    }
}