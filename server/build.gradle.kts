
plugins {
    id("io.vertx.vertx-plugin") version "1.3.0"
    id("com.google.cloud.tools.jib") version "3.1.2"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
}

repositories {
    mavenCentral()
}

val junitVersion = "5.3.2"
val exposedVersion = "0.32.1"

dependencies {
    implementation(project(":ontology"))

    implementation("io.vertx:vertx-core")
    implementation("io.vertx:vertx-lang-kotlin")
    implementation("io.vertx:vertx-lang-kotlin-coroutines")
    implementation("io.vertx:vertx-web-graphql")
    implementation("io.vertx:vertx-auth-jwt")
    implementation("io.vertx:vertx-jdbc-client")
    implementation("io.vertx:vertx-config")
    implementation("io.vertx:vertx-auth-jdbc")

    implementation("org.apache.logging.log4j:log4j-core:2.14.1")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.14.1")

    implementation("com.expediagroup:graphql-kotlin-schema-generator:4.2.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.3")

    implementation("com.google.guava:guava:30.1.1-jre")

    implementation("org.flywaydb:flyway-core:7.9.1")
    implementation("io.agroal:agroal-pool:1.11")
    implementation("org.postgresql:postgresql:42.2.20")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("io.vertx:vertx-json-schema:4.1.3")

    implementation("com.apicatalog:titanium-json-ld:1.1.0")
    implementation("org.glassfish:jakarta.json:2.0.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.1")

    implementation("com.github.jsonld-java:jsonld-java:0.13.3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jakarta-jsonp:2.12.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.12.2")

    implementation("com.github.slugify:slugify:2.4")

    testImplementation("io.vertx:vertx-junit5")
    testImplementation("io.vertx:vertx-web-client")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.mockito:mockito-core:3.+")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    testImplementation("org.testcontainers:testcontainers:1.16.0")
    testImplementation("org.testcontainers:postgresql:1.16.0")
    testImplementation("org.testcontainers:junit-jupiter:1.16.0")
}

vertx {
    mainVerticle = "dev.scalar.trawler.server.App"
    vertxVersion = "4.1.3"
}

tasks.test {
    useJUnitPlatform()
}

jib {
    from {
        image = "gcr.io/distroless/java:11"
    }

    to {
        image = "scalardev/trawler"
    }

    container {
        mainClass = "io.vertx.core.Launcher"
        args = listOf("run", "dev.scalar.trawler.server.App")
    }
}
