package dev.scalar.trawler.server.verticle

import dev.scalar.trawler.server.auth.PermissiveJWTAuthHandler
import dev.scalar.trawler.server.auth.Users
import dev.scalar.trawler.server.auth.mintToken
import dev.scalar.trawler.server.graphql.QueryContext
import dev.scalar.trawler.server.graphql.makeSchema
import graphql.GraphQL
import io.vertx.ext.auth.jdbc.JDBCAuthentication
import io.vertx.ext.auth.jdbc.JDBCAuthenticationOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.common.WebEnvironment
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.ext.web.handler.graphql.GraphQLHandler
import io.vertx.ext.web.handler.graphql.GraphiQLHandler
import io.vertx.ext.web.handler.graphql.GraphiQLHandlerOptions
import org.apache.logging.log4j.LogManager
import java.util.UUID

class GraphQLApi : BaseVerticle() {
    private val log = LogManager.getLogger()

    override suspend fun start() {
        super.start()
        configureDatabase()

        val router = Router.router(vertx)
        val jdbcAuth = JDBCAuthentication.create(
            jdbcClient(vertx),
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
                .setHeaders(mapOf("Authorization" to "Bearer ${mintToken(jwtAuth, Users.DEV)}"))
            router.route("/graphiql/*").handler(GraphiQLHandler.create(options))
        } else {
            router.route("/graphiql/*").handler(GraphiQLHandler.create())
        }

        router
            .route()
            .handler(PermissiveJWTAuthHandler(jwtAuth))
            .handler(CorsHandler.create(".*."))
            .handler(
                GraphQLHandler.create(GraphQL.newGraphQL(makeSchema()).build())
                    .queryContext { rc ->
                        val accountId = rc.user()?.principal()?.getString("sub")

                        QueryContext(
                            rc.user(),
                            if (accountId != null) UUID.fromString(accountId) else UUID(0, 0),
                            jdbcAuth,
                            jwtAuth
                        )
                    }
            )

        log.info("Starting graphql API")
        vertx.createHttpServer().requestHandler(router).listen(8080)
    }
}
