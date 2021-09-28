package dev.scalar.trawler.server.ontology

import java.lang.IllegalArgumentException
import java.util.*

class OntologyImpl(
    entityTypes: Set<EntityType>,
    facetTypes: Set<FacetType>,
    urnSpaces: Set<URNSpace>
): Ontology {
    private val urnSpaces = urnSpaces.associate { space -> space.name to space }
    private val entityTypesByUri = entityTypes.associateBy { entityType -> entityType.uri }

    private val facetTypesByUri = facetTypes.associateBy { facetType -> facetType.uri }
    private val facetTypesById = facetTypes.associateBy { facetType -> facetType.id }

    fun parseUrn(urn: String): ParsedUrn {
        /*
        urn:tr:<project>:<space>:<segment>:<path>
        */

        val bits = urn.split(":")

        assert(bits.size == 6)

        assert(bits[0] == "urn")

        val namespace = bits[1]

        val projectId = if (bits[2].isNotEmpty()) {
           UUID.fromString(bits[2])
        } else {
            null
        }

        val space: URNSpace = if (urnSpaces.containsKey(bits[3])) {
            urnSpaces[bits[3]]!!
        } else {
            throw IllegalArgumentException("Unrecognised URN space: ${bits[3]}")
        }

        val segment = bits[4]
        val path = bits[5]

        return ParsedUrn(
           namespace,
           projectId,
           emptyMap(),
            bits[3],
            space.parse(path)
        )
    }

    override fun entityTypeByUri(uri: String): EntityType? = entityTypesByUri[uri]

    override fun facetTypeByUri(uri: String): FacetType? = facetTypesByUri[uri]

    override fun facetTypeById(id: UUID): FacetType? = facetTypesById[id]
}