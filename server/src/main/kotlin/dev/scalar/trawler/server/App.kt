package dev.scalar.trawler.server

import com.fasterxml.jackson.datatype.jsonp.JSONPModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import dev.scalar.trawler.server.db.EntityType
import dev.scalar.trawler.server.db.FacetType
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
import java.util.*

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

        newSuspendedTransaction {
            EntityType.insertIgnore {
                it[EntityType.uri] = "http://trawler.dev/schema/core#SqlDatabase"
                it[EntityType.name] = "SqlDatabase"
            }

            EntityType.insertIgnore {
                it[EntityType.uri] = "http://trawler.dev/schema/core#SqlTable"
                it[EntityType.name] = "SqlTable"
            }

            EntityType.insertIgnore {
                it[EntityType.uri] = "http://trawler.dev/schema/core#SqlColumn"
                it[EntityType.name] = "SqlColumn"
            }

            FacetType.insertIgnore {
                it[FacetType.uri] = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
                it[FacetType.metaType] = "type_reference"
                it[FacetType.name] = "Type"
            }

            FacetType.insertIgnore {
                it[FacetType.uri] = "http://schema.org/name"
                it[FacetType.metaType] = "string"
                it[FacetType.name] = "Name"
            }

            FacetType.insertIgnore {
                it[FacetType.uri] = "http://trawler.dev/schema/core#has"
                it[FacetType.metaType] = "relationship"
                it[FacetType.name] = "Has"
            }

            FacetType.insertIgnore {
                it[FacetType.uri] = "http://trawler.dev/schema/metrics#nullRatio"
                it[FacetType.metaType] = "double"
                it[FacetType.name] = "Null Ratio"
            }

            FacetType.insertIgnore {
                it[FacetType.uri] = "http://trawler.dev/schema/metrics#count"
                it[FacetType.metaType] = "double"
                it[FacetType.name] = "Count"
            }

            FacetType.insertIgnore {
                it[FacetType.uri] = "http://trawler.dev/schema/metrics#max"
                it[FacetType.metaType] = "double"
                it[FacetType.name] = "Max"
            }

            FacetType.insertIgnore {
                it[FacetType.uri] = "http://trawler.dev/schema/metrics#min"
                it[FacetType.metaType] = "double"
                it[FacetType.name] = "Min"
            }

            FacetType.insertIgnore {
                it[FacetType.uri] = "http://trawler.dev/schema/core#type"
                it[FacetType.metaType] = "string"
                it[FacetType.name] = "Type"
            }

            FacetType.insertIgnore {
                it[FacetType.uri] = "http://trawler.dev/schema/core#isNullable"
                it[FacetType.metaType] = "boolean"
                it[FacetType.name] = "Is Nullable"
            }
        }

        vertx.deployVerticle(CollectJsonApi::class.java, DeploymentOptions().setWorker(true))
            .onFailure { log.error("Failed to deploy collect API", it) }

        vertx.deployVerticle(Indexer::class.java, DeploymentOptions().setWorker(true))
            .onFailure { log.error("Failed to deploy indexer API", it) }

        vertx.deployVerticle(GraphQLApi::class.java, DeploymentOptions())
            .onFailure { log.error("Failed to deploy graphql API", it) }
    }
}