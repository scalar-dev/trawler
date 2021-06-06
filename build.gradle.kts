plugins {
  id("org.jetbrains.kotlin.jvm") version "1.5.0" apply false
}

subprojects {
  apply(plugin = "org.jetbrains.kotlin.jvm")
}

repositories {
  mavenCentral()
}

