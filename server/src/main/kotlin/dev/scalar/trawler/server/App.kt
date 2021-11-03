package dev.scalar.trawler.server

import dev.scalar.trawler.server.collect.ApiKeyAuthProvider
import dev.scalar.trawler.server.db.createGuestUser
import dev.scalar.trawler.server.db.devProject
import dev.scalar.trawler.server.db.devUser
import dev.scalar.trawler.server.db.updateOntology
import dev.scalar.trawler.server.verticle.CollectApi
import dev.scalar.trawler.server.verticle.Config
import dev.scalar.trawler.server.verticle.GraphQLApi
import dev.scalar.trawler.server.verticle.Indexer
import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.DeploymentOptions
import io.vertx.core.Verticle
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.web.common.WebEnvironment
import io.vertx.jdbcclient.JDBCConnectOptions
import io.vertx.jdbcclient.impl.AgroalCPDataSourceProvider
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.sqlclient.PoolOptions
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.flywaydb.core.Flyway
import javax.sql.DataSource

class App : CoroutineVerticle() {
    companion object App {
        fun configureDatabase(config: JsonObject): DataSource {
            val host = config.getString(Config.PGHOST, "localhost")
            val port = config.getInteger(Config.PGPORT, 54321)
            val database = config.getString(Config.PGDATABASE, "postgres")
            val username = config.getString(Config.PGUSER, "postgres")
            val password = config.getString(Config.PGPASSWORD, "postgres")

            val connectOptions = JDBCConnectOptions()
                .setJdbcUrl("jdbc:postgresql://$host:$port/$database")
                .setUser(username)
                .setPassword(password)

            val poolOptions = PoolOptions()
                .setMaxSize(16)

            val dataSource = AgroalCPDataSourceProvider(connectOptions, poolOptions)
                .getDataSource(null)

            Flyway.configure().dataSource(dataSource).locations("classpath:migrations")
                .load()
                .migrate()

            org.jetbrains.exposed.sql.Database.connect(dataSource)

            return dataSource
        }
    }

    private val log: Logger = LogManager.getLogger()

    private fun deployVerticle(verticle: Verticle, config: JsonObject, worker: Boolean) = vertx
        .deployVerticle(verticle, DeploymentOptions().setWorker(worker).setConfig(config))
        .onFailure { log.error("Failed to deploy ${verticle::class.qualifiedName}", it) }

    private fun configRetriever() = ConfigRetriever.create(
        vertx,
        ConfigRetrieverOptions()
            .addStore(ConfigStoreOptions().setType("env"))
    )

    fun collectApi(dataSource: DataSource) = CollectApi(
        ApiKeyAuthProvider(JDBCClient.create(vertx, dataSource))
    )

    fun indexer() = Indexer()

    fun graphQLApi(dataSource: DataSource) = GraphQLApi(dataSource)

    suspend fun dbDefaults() {
        createGuestUser()

        if (WebEnvironment.development()) {
            devProject()
            devUser()
        }

        log.info("Updating root ontology")
        updateOntology(vertx)
    }

    override suspend fun start() {
        val verticleConfig = awaitResult<JsonObject> { h -> configRetriever().getConfig(h) }

        log.info("Connecting to database")
        val dataSource = configureDatabase(verticleConfig)

        dbDefaults()

        val collectApi = collectApi(dataSource)
        deployVerticle(collectApi, config, true)

        val indexer = indexer()
        deployVerticle(indexer, config, true)

        val graphQLApi = graphQLApi(dataSource)
        deployVerticle(graphQLApi, config, false)
    }
}
