
plugins {
  id("io.vertx.vertx-plugin") version "1.2.0"
}

repositories {
  mavenCentral()
}

val junitVersion = "5.3.2"
val exposedVersion = "0.31.1"

dependencies {
  implementation(project(":proto"))
  implementation("io.vertx:vertx-core")
  implementation("io.vertx:vertx-lang-kotlin")
  implementation("io.vertx:vertx-lang-kotlin-coroutines")
  implementation("io.vertx:vertx-web-graphql")
  implementation("io.vertx:vertx-auth-jwt")
  implementation("io.vertx:vertx-grpc")

  implementation("org.apache.logging.log4j:log4j-core:2.14.1")
  implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.14.1")

  implementation("com.expediagroup:graphql-kotlin-schema-generator:4.1.1")
  implementation("com.fasterxml.jackson.core:jackson-databind")

  implementation("io.vertx:vertx-jdbc-client:4.0.3")
  implementation("io.agroal:agroal-pool:1.11")
  implementation("org.postgresql:postgresql:42.2.20")
  implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
  implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
  implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
  implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

  testImplementation("io.vertx:vertx-junit5")
  testImplementation("io.vertx:vertx-web-client")
  testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

vertx {
  mainVerticle = "dev.scalar.trawler.server.App"
  vertxVersion = "4.0.3"
}

tasks.test {
  useJUnitPlatform()
}
