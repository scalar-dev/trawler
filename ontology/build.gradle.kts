
repositories {
  mavenCentral()
}

val junitVersion = "5.3.2"

dependencies {
  implementation("io.vertx:vertx-core")
  implementation("io.vertx:vertx-json-schema:4.1.5")
  implementation("org.apache.logging.log4j:log4j-core:2.14.1")
  implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.14.1")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.12.3")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.3")
  implementation("com.google.guava:guava:30.1.1-jre")
  implementation("org.glassfish:jakarta.json:2.0.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.1")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jakarta-jsonp:2.12.2")

  testImplementation("io.vertx:vertx-junit5")
  testImplementation("io.vertx:vertx-web-client")
  testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.test {
  useJUnitPlatform()
}
