package dev.scalar.trawler.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.datatype.jsonp.JSONPModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.readValue
import dev.scalar.trawler.ontology.config.OntologyConfig
import dev.scalar.trawler.server.db.EntityType
import dev.scalar.trawler.server.db.FacetType
import dev.scalar.trawler.server.ontology.OntologyUpload
import io.vertx.core.DeploymentOptions
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.jdbcclient.JDBCConnectOptions
import io.vertx.jdbcclient.impl.AgroalCPDataSourceProvider
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.sqlclient.PoolOptions
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class App : CoroutineVerticle() {
    private val log: Logger = LogManager.getLogger()

    override suspend fun start() {
        val connectOptions = JDBCConnectOptions() // H2 connection string
            .setJdbcUrl("jdbc:postgresql://localhost:54321/postgres") // username
            .setUser("postgres") // password
            .setPassword("postgres")  // configure the pool

        val poolOptions = PoolOptions()
            .setMaxSize(16)

        val dataSource = AgroalCPDataSourceProvider(connectOptions, poolOptions).getDataSource(null)

        Flyway.configure().dataSource(dataSource).locations("classpath:migrations")
            .load()
            .migrate()

        DatabindCodec.mapper()
            .registerModule(KotlinModule())
            .registerModule(JSONPModule())

        log.info("Connecting to database")
        Database.connect(dataSource)

        // Update the core ontology
        val coreOntology = ObjectMapper(YAMLFactory())
            .registerModule(KotlinModule())
            .readValue<OntologyConfig>(App::class.java.getResourceAsStream("/core.ontology.yml"))
        OntologyUpload().upload(null, coreOntology)

        vertx.deployVerticle(CollectJsonApi::class.java, DeploymentOptions().setWorker(true))
            .onFailure { log.error("Failed to deploy collect API", it) }

        vertx.deployVerticle(Indexer::class.java, DeploymentOptions().setWorker(true))
            .onFailure { log.error("Failed to deploy indexer API", it) }

        vertx.deployVerticle(GraphQLApi::class.java, DeploymentOptions())
            .onFailure { log.error("Failed to deploy graphql API", it) }
    }
}