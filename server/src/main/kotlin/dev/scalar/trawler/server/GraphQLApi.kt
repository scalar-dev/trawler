package dev.scalar.trawler.server

import dev.scalar.trawler.server.auth.PermissiveJWTAuthHandler
import dev.scalar.trawler.server.auth.jwtAuth
import dev.scalar.trawler.server.db.Database
import dev.scalar.trawler.server.db.Project.DEMO_PROJECT_ID
import dev.scalar.trawler.server.db.devUserToken
import dev.scalar.trawler.server.graphql.QueryContext
import dev.scalar.trawler.server.graphql.makeSchema
import graphql.GraphQL
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jdbc.JDBCAuthentication
import io.vertx.ext.auth.jdbc.JDBCAuthenticationOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.Router
import io.vertx.ext.web.common.WebEnvironment
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.graphql.GraphQLHandler
import io.vertx.ext.web.handler.graphql.GraphiQLHandler
import io.vertx.ext.web.handler.graphql.GraphiQLHandlerOptions
import io.vertx.kotlin.coroutines.CoroutineVerticle
import org.apache.logging.log4j.LogManager
import java.util.UUID

class GraphQLApi : CoroutineVerticle() {
    private val log = LogManager.getLogger()

    override suspend fun start() {
        val router = Router.router(vertx)
        val jwtAuth: JWTAuth = jwtAuth(vertx)
        val jdbcAuth = JDBCAuthentication.create(
            Database.jdbcClient(vertx),
            JDBCAuthenticationOptions().setAuthenticationQuery(
                "SELECT PASSWORD FROM account WHERE id = ?::UUID"
            )
        )

        router
            .errorHandler(500) { rc ->
                rc.failure().printStackTrace()
                rc.json(
                    mapOf("message" to rc.failure().message)
                )
                rc.fail(403)
            }
            .route()
            .handler(BodyHandler.create())

        if (WebEnvironment.development()) {
            val options = GraphiQLHandlerOptions().setEnabled(true)
                .setHeaders(mapOf("Authorization" to "Bearer ${devUserToken(jwtAuth)}"))
            router.route("/graphiql/*").handler(GraphiQLHandler.create(options))
        }

        router
            .route()
            .handler(PermissiveJWTAuthHandler(jwtAuth))
            .handler(
                GraphQLHandler.create(GraphQL.newGraphQL(makeSchema()).build())
                    .queryContext { rc ->
                        val accountId = rc.user()?.principal()?.getString("sub")

                        QueryContext(
                            rc.user(),
                            if (accountId != null) UUID.fromString(accountId) else null,
                            jdbcAuth,
                            jwtAuth
                        )
                    }
            )

        log.info("Starting graphql API")
        vertx.createHttpServer().requestHandler(router).listen(8080)
    }
}
