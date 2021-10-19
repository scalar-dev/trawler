package dev.scalar.trawler.server.db

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import dev.scalar.trawler.ontology.config.OntologyConfig
import dev.scalar.trawler.server.App
import dev.scalar.trawler.server.auth.Users
import dev.scalar.trawler.server.ontology.OntologyUpload
import dev.scalar.trawler.server.verticle.Config
import io.vertx.core.Vertx
import io.vertx.ext.web.common.WebEnvironment
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

suspend fun updateOntology(vertx: Vertx) {
    // Update the core ontology
    val coreOntology = ObjectMapper(YAMLFactory())
        .registerModule(KotlinModule())
        .readValue<OntologyConfig>(App::class.java.getResourceAsStream("/core.ontology.yml"))
    OntologyUpload(vertx).upload(null, coreOntology)
}

suspend fun devProject() = newSuspendedTransaction {
    newSuspendedTransaction {
        Project.insertIgnore {
            it[Project.id] = DEMO_PROJECT_ID
            it[Project.name] = "Demo project"
            it[Project.slug] = "test"
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

suspend fun devUser() = newSuspendedTransaction {
    Account.insertIgnore {
        it[Account.id] = Users.DEV
        it[Account.password] = "NO_LOGIN"
    }

    AccountInfo.insertIgnore {
        it[AccountInfo.accountId] = Users.DEV
        it[AccountInfo.email] = "dev@trawler.dev"
    }

    AccountRole.insertIgnore {
        it[AccountRole.accountId] = Users.DEV
        it[AccountRole.projectId] = Project.DEMO_PROJECT_ID
        it[AccountRole.role] = "admin"
    }
}

fun devSecret() = if (WebEnvironment.development()) {
    "keyboard cat"
} else {
    throw Exception("Please supply a valid value for ${Config.TRAWLER_SECRET}")
}
