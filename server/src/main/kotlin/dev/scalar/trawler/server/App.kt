package dev.scalar.trawler.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.datatype.jsonp.JSONPModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import dev.scalar.trawler.ontology.config.OntologyConfig
import dev.scalar.trawler.server.db.Database
import dev.scalar.trawler.server.db.Project
import dev.scalar.trawler.server.db.createGuestUser
import dev.scalar.trawler.server.ontology.OntologyUpload
import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.DeploymentOptions
import io.vertx.core.Verticle
import io.vertx.core.json.JsonObject
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.ext.web.common.WebEnvironment
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitResult
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class App : CoroutineVerticle() {
    private val log: Logger = LogManager.getLogger()

    private inline fun <reified T : Verticle> deployVerticle(config: JsonObject, worker: Boolean) = vertx
        .deployVerticle(T::class.java, DeploymentOptions().setWorker(worker).setConfig(config))
        .onFailure { log.error("Failed to deploy ${T::class.qualifiedName}", it) }

    private suspend fun updateOntology() {
        // Update the core ontology
        val coreOntology = ObjectMapper(YAMLFactory())
            .registerModule(KotlinModule())
            .readValue<OntologyConfig>(App::class.java.getResourceAsStream("/core.ontology.yml"))
        OntologyUpload().upload(null, coreOntology)
    }

    private fun configRetriever() = ConfigRetriever.create(
        vertx,
        ConfigRetrieverOptions()
            .addStore(ConfigStoreOptions().setType("env"))
    )

    override suspend fun start() {
        DatabindCodec.mapper()
            .registerModule(KotlinModule())
            .registerModule(JSONPModule())

        val config = awaitResult<JsonObject> { h -> configRetriever().getConfig(h) }
        Database.configure(config)

        createGuestUser()

        if (WebEnvironment.development()) {
            newSuspendedTransaction {
                Project.insertIgnore {
                    it[Project.id] = DEMO_PROJECT_ID
                    it[Project.name] = "Test"
                }
            }
        }

        log.info("Updating root ontology")
        updateOntology()

        deployVerticle<CollectJsonApi>(config, true)
        deployVerticle<Indexer>(config, true)
        deployVerticle<GraphQLApi>(config, false)
    }
}
