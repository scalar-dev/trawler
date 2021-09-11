package dev.scalar.trawler.server.schema

import java.util.*

interface TypeRegistry {
    suspend fun entityTypeByUri(uri: String): EntityType?
    suspend fun facetTypeByUri(uri: String): FacetType?
    suspend fun entityTypeByUrn(urn: String): EntityType?
}