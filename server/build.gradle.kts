
plugins {
  id("io.vertx.vertx-plugin") version "1.3.0"
}

repositories {
  mavenCentral()
}

val junitVersion = "5.3.2"
val exposedVersion = "0.32.1"

dependencies {
  implementation("io.vertx:vertx-core")
  implementation("io.vertx:vertx-lang-kotlin")
  implementation("io.vertx:vertx-lang-kotlin-coroutines")
  implementation("io.vertx:vertx-web-graphql")
  implementation("io.vertx:vertx-auth-jwt")
  implementation("io.vertx:vertx-jdbc-client")

  implementation("org.apache.logging.log4j:log4j-core:2.14.1")
  implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.14.1")

  implementation("com.expediagroup:graphql-kotlin-schema-generator:4.2.0")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.12.3")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.3")

  implementation("org.flywaydb:flyway-core:7.9.1")
  implementation("io.agroal:agroal-pool:1.11")
  implementation("org.postgresql:postgresql:42.2.20")
  implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
  implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
  implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
  implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

  implementation("com.apicatalog:titanium-json-ld:1.1.0")
  implementation("org.glassfish:jakarta.json:2.0.0")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.1")

  implementation("com.github.jsonld-java:jsonld-java:0.13.3")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jakarta-jsonp:2.12.2")

  testImplementation("io.vertx:vertx-junit5")
  testImplementation("io.vertx:vertx-web-client")
  testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

vertx {
  mainVerticle = "dev.scalar.trawler.server.App"
  vertxVersion = "4.1.3"
}

tasks.test {
  useJUnitPlatform()
}