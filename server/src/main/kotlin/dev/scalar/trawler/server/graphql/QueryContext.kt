package dev.scalar.trawler.server.graphql

import com.expediagroup.graphql.generator.execution.GraphQLContext
import dev.scalar.trawler.server.collect.ApiKeyAuthProvider
import dev.scalar.trawler.server.db.AccountRole
import dev.scalar.trawler.server.db.Project
import dev.scalar.trawler.server.ontology.OntologyCache
import io.vertx.ext.auth.User
import io.vertx.ext.auth.jdbc.JDBCAuthentication
import io.vertx.ext.auth.jwt.JWTAuth
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

data class QueryContext(
    val user: User?,
    val accountId: UUID,
    val jdbcAuthentication: JDBCAuthentication,
    val jwtAuth: JWTAuth,
    val apiAuth: ApiKeyAuthProvider,
    val ontologyCache: OntologyCache
) : GraphQLContext {
    suspend fun projectId(project: String, role: String? = null) = newSuspendedTransaction {
        Project
            .innerJoin(AccountRole)
            .slice(Project.id)
            .select {
                val q = Project.slug.eq(project) and
                    AccountRole.accountId.eq(this@QueryContext.accountId)

                if (role != null) {
                    q and AccountRole.role.eq(role)
                } else {
                    q
                }
            }
            .firstOrNull()?.get(Project.id)?.value
    } ?: throw Exception("Project not found: $project")
}
