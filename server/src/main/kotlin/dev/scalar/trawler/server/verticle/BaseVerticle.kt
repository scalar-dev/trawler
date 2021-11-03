package dev.scalar.trawler.server.verticle

import com.fasterxml.jackson.datatype.jsonp.JSONPModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import dev.scalar.trawler.server.auth.jwtAuth
import dev.scalar.trawler.server.db.createGuestUser
import dev.scalar.trawler.server.db.devApiKey
import dev.scalar.trawler.server.db.devProject
import dev.scalar.trawler.server.db.devSecret
import dev.scalar.trawler.server.db.devUser
import dev.scalar.trawler.server.db.updateOntology
import io.vertx.core.Vertx
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

    protected val jwtAuth by lazy {
        jwtAuth(
            vertx,
            config.getString(Config.TRAWLER_SECRET) ?: devSecret(),
            30 * 24 * 60 * 60
        )
    }




    override suspend fun start() {
        DatabindCodec.mapper()
            .registerModule(KotlinModule())
            .registerModule(JSONPModule())
    }
}
