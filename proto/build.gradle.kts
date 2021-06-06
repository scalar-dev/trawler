import com.google.protobuf.gradle.*
import com.google.protobuf.gradle.plugins
import org.gradle.kotlin.dsl.protobuf

plugins {
    id("com.google.protobuf") version "0.8.14"
}

version = "0.1"
group = "yak.proto"

val grpcKotlinVersion= project.properties["grpcKotlinVersion"]
val grpcVersion= project.properties["grpcVersion"]
val protocVersion= project.properties["protocVersion"]

repositories {
    mavenCentral()
    jcenter()
    google()
}

dependencies {
    api("io.grpc:grpc-kotlin-stub:${grpcKotlinVersion}")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")

    implementation("javax.annotation:javax.annotation-api:1.3.2")
}

java {
    sourceCompatibility = JavaVersion.toVersion("1.8")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/grpc")
            srcDirs("build/generated/source/proto/main/grpckt")
            srcDirs("build/generated/source/proto/main/java")
        }
    }
}

protobuf {
    protobuf.protoc {
        artifact ="com.google.protobuf:protoc:${protocVersion}"
    }

    protobuf.plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${grpcVersion}"
        }

        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:${grpcKotlinVersion}:jdk7@jar"
        }
    }

    protobuf.generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("grpc")
                id("grpckt")
            }
        }
    }
}

