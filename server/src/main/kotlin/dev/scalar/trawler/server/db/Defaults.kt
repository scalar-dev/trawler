package dev.scalar.trawler.server.db

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import dev.scalar.trawler.ontology.config.OntologyConfig
import dev.scalar.trawler.server.App
import dev.scalar.trawler.server.ontology.OntologyUpload
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.JWTAuth
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

suspend fun updateOntology() {
    // Update the core ontology
    val coreOntology = ObjectMapper(YAMLFactory())
        .registerModule(KotlinModule())
        .readValue<OntologyConfig>(App::class.java.getResourceAsStream("/core.ontology.yml"))
    OntologyUpload().upload(null, coreOntology)
}

suspend fun devProject() = newSuspendedTransaction {
    newSuspendedTransaction {
        Project.insertIgnore {
            it[Project.id] = DEMO_PROJECT_ID
            it[Project.name] = "Demo project"
        }
    }
}

suspend fun createGuestUser() = newSuspendedTransaction {
    val id = UUID(0, 0)
    Account.insertIgnore {
        it[Account.id] = id
        it[Account.password] = "NO_LOGIN"
    }
}

suspend fun devUserToken(jwtAuth: JWTAuth) = newSuspendedTransaction {
    val id = UUID(0, 1)
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
