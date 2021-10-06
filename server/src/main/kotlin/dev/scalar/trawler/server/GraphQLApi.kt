package dev.scalar.trawler.server

import dev.scalar.trawler.server.auth.PermissiveJWTAuthHandler
import dev.scalar.trawler.server.auth.jwtAuth
import dev.scalar.trawler.server.db.Database
import dev.scalar.trawler.server.db.Project.DEMO_PROJECT_ID
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
            val devToken = jwtAuth.generateToken(
                JsonObject(
                    mapOf(
                        "sub" to "devuser",
                        "project" to DEMO_PROJECT_ID.toString()
                    )
                )
            )

            val options = GraphiQLHandlerOptions().setEnabled(true)
                .setHeaders(mapOf("Authorization" to "Bearer $devToken"))
            router.route("/graphiql/*").handler(GraphiQLHandler.create(options))
        }

        router
            .route()
            .handler(PermissiveJWTAuthHandler(jwtAuth))
            .handler(
                GraphQLHandler.create(GraphQL.newGraphQL(makeSchema()).build())
                    .queryContext { rc ->
                        QueryContext(rc.user(), DEMO_PROJECT_ID, jdbcAuth, jwtAuth)
                    }
            )

        log.info("Starting graphql API")
        vertx.createHttpServer().requestHandler(router).listen(8080)
    }
}
