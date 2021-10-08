package dev.scalar.trawler.server.db

import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.JWTAuth
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

suspend fun devProject() = newSuspendedTransaction {
    newSuspendedTransaction {
        Project.insertIgnore {
            it[Project.id] = DEMO_PROJECT_ID
            it[Project.name] = "Demo project"
        }
    }
}

suspend fun devUserToken(jwtAuth: JWTAuth) = newSuspendedTransaction {
    val id = UUID(0, 0 )
    Account.insertIgnore {
        it[Account.id] = id
        it[Account.password] = "NO_LOGIN"
    }

    AccountInfo.insertIgnore {
        it[AccountInfo.accountId] = id
        it[AccountInfo.email] = "dev@trawler.dev"
    }

    AccountRole.insertIgnore {
        it[AccountRole.accountId] = id
        it[AccountRole.projectId] = Project.DEMO_PROJECT_ID
        it[AccountRole.role] = "admin"
    }

    jwtAuth.generateToken(
        JsonObject(
            mapOf(
                "sub" to id.toString(),
            )
        )
    )
}
