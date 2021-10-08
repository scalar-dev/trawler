package dev.scalar.trawler.server.graphql

import com.expediagroup.graphql.generator.execution.GraphQLContext
import io.vertx.ext.auth.User
import io.vertx.ext.auth.jdbc.JDBCAuthentication
import io.vertx.ext.auth.jwt.JWTAuth
import java.util.UUID

data class QueryContext(
    val user: User?,
    val accountId: UUID,
    val jdbcAuthentication: JDBCAuthentication,
    val jwtAuth: JWTAuth
) : GraphQLContext
