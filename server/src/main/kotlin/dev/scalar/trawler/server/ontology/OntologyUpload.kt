package dev.scalar.trawler.server.ontology

import dev.scalar.trawler.ontology.config.OntologyConfig
import dev.scalar.trawler.server.db.EntityType
import dev.scalar.trawler.server.db.FacetType
import dev.scalar.trawler.server.db.util.set
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.util.*

class OntologyUpload {
    val log = LogManager.getLogger()

    suspend fun upload(projectId: UUID?, config: OntologyConfig) = newSuspendedTransaction {
        val existingOntology = loadOntology(projectId)

        log.info("Uploading ontology $projectId")

        existingOntology.entityTypes().forEach { entityType ->
            val match = config.entityTypes.find { it.uri == entityType.uri }

            if (match == null && entityType.projectId == projectId) {
                log.warn("Entity type ${entityType.uri} is not present in ontology. Deprecating")
                EntityType.update({ EntityType.id.eq(entityType.id) }) {
                    it[EntityType.isDeprecated] = true
                }
            }
        }

        config.entityTypes.forEach { entityType ->
            val existing = existingOntology.entityTypes()
                .find { it.uri == entityType.uri }

            if (existing == null) {
                log.info("Creating entity type: ${entityType.uri}")
                dev.scalar.trawler.server.db.EntityType.insert {
                    it[dev.scalar.trawler.server.db.EntityType.uri] = entityType.uri
                    it[dev.scalar.trawler.server.db.EntityType.name] = entityType.name
                    it[dev.scalar.trawler.server.db.EntityType.projectId] = projectId!!
                }
            } else if (existing.projectId == projectId) {
                log.info("Updating entity type: ${entityType.uri}")
                dev.scalar.trawler.server.db.EntityType.update({ dev.scalar.trawler.server.db.EntityType.id.eq(existing.id) }) {
                    it[dev.scalar.trawler.server.db.EntityType.name] = entityType.name
                    it[dev.scalar.trawler.server.db.EntityType.isDeprecated] = false
                }
            } else {
                log.info("Skipping ${entityType.uri}")
            }
        }

        existingOntology.facetTypes()
            .forEach { facetType ->
                val match = config.facetTypes.find { it.uri == facetType.uri }

                if (match == null && facetType.projectId == projectId) {
                    log.warn("Facet type ${facetType.uri} is not present in ontology. Deprecating")
                    FacetType.update({ EntityType.id.eq(facetType.id) }) {
                        it[FacetType.isDeprecated] = true
                    }
                }
            }

        config.facetTypes.forEach { facetType ->
            val existing = existingOntology.facetTypes().find { it.uri == facetType.uri }

            if (existing == null) {
                log.info("Creating facet type: ${facetType.uri}")
                dev.scalar.trawler.server.db.FacetType.insert {
                    it[dev.scalar.trawler.server.db.FacetType.uri] = facetType.uri
                    it[dev.scalar.trawler.server.db.FacetType.name] = facetType.name
                    it[dev.scalar.trawler.server.db.FacetType.metaType] = facetType.metaType.value
                    it[dev.scalar.trawler.server.db.FacetType.projectId] = projectId
                }
            } else if (existing.projectId == projectId) {
                log.info("Updating facet type: ${facetType.uri}")
                dev.scalar.trawler.server.db.FacetType.update({ dev.scalar.trawler.server.db.FacetType.id.eq(existing.id) }) {
                    it[dev.scalar.trawler.server.db.FacetType.name] = facetType.name
                    it[dev.scalar.trawler.server.db.FacetType.isDeprecated] = false
                }
            } else {
                log.info("Skipping ${facetType.uri}")
            }
        }
    }
}
