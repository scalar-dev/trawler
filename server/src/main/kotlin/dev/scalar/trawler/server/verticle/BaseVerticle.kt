package dev.scalar.trawler.server.verticle

import com.fasterxml.jackson.datatype.jsonp.JSONPModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import dev.scalar.trawler.server.db.createGuestUser
import dev.scalar.trawler.server.db.devProject
import dev.scalar.trawler.server.db.updateOntology
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.web.common.WebEnvironment
import io.vertx.jdbcclient.JDBCConnectOptions
import io.vertx.jdbcclient.impl.AgroalCPDataSourceProvider
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.sqlclient.PoolOptions
import org.apache.logging.log4j.LogManager
import org.flywaydb.core.Flyway
import javax.sql.DataSource

abstract class BaseVerticle : CoroutineVerticle() {
    private val log = LogManager.getLogger()
    private lateinit var dataSource: DataSource

    fun configureDatabase(config: JsonObject) {
        val host = config.getString("PGHOST", "localhost")
        val port = config.getInteger("PGPORT", 54321)
        val database = config.getString("PGDATABASE", "postgres")
        val username = config.getString("PGUSER", "postgres")
        val password = config.getString("PGPASSWORD", "postgres")

        val connectOptions = JDBCConnectOptions() // H2 connection string
            .setJdbcUrl("jdbc:postgresql://$host:$port/$database") // username
            .setUser(username) // password
            .setPassword(password) // configure the pool

        val poolOptions = PoolOptions()
            .setMaxSize(16)

        val dataSource = AgroalCPDataSourceProvider(connectOptions, poolOptions)
            .getDataSource(null)

        Flyway.configure().dataSource(dataSource).locations("classpath:migrations")
            .load()
            .migrate()

        log.info("Connecting to database")
        org.jetbrains.exposed.sql.Database.connect(dataSource)

        this.dataSource = dataSource
    }

    suspend fun dbDefaults() {
        createGuestUser()

        if (WebEnvironment.development()) {
            devProject()
        }

        log.info("Updating root ontology")
        updateOntology()
    }

    fun jdbcClient(vertx: Vertx) = JDBCClient.create(vertx, dataSource)

    override suspend fun start() {
        DatabindCodec.mapper()
            .registerModule(KotlinModule())
            .registerModule(JSONPModule())
    }
}
