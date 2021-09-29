package dev.scalar.trawler.server.graphql

import com.expediagroup.graphql.generator.execution.GraphQLContext
import io.vertx.ext.auth.User
import java.util.*

data class QueryContext(val user: User, val projectId: UUID) : GraphQLContext