package dev.scalar.trawler.server.db

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.jdbcclient.JDBCConnectOptions
import io.vertx.jdbcclient.impl.AgroalCPDataSourceProvider
import io.vertx.sqlclient.PoolOptions
import org.apache.logging.log4j.LogManager
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import javax.sql.DataSource

object Database {
    private val log = LogManager.getLogger()
    private lateinit var dataSource: DataSource

    fun configure(config: JsonObject) {
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
        Database.connect(dataSource)

        this.dataSource = dataSource
    }

    fun jdbcClient(vertx: Vertx) = JDBCClient.create(vertx, dataSource)
}
