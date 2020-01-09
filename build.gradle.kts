plugins {
    kotlin("multiplatform") version Jetbrains.Kotlin.version apply false
    id("kotlinx-serialization") version Jetbrains.Kotlin.version apply false
//    id("org.jetbrains.kotlin.frontend") version "0.0.45" apply false
}

allprojects {
    group = "moe.nikky.penta"

    // TODO: remove
    repositories {
        mavenCentral()
        jcenter()
        maven(url = "https://dl.bintray.com/kotlin/kotlinx") {
            name = "kotlinx"
        }
        // TODO: do we need that here ?
        maven(url = "https://dl.bintray.com/kotlin/ktor") {
            name = "ktor"
        }
        maven(url = "https://dl.bintray.com/data2viz/data2viz/") {
            name = "d2v"
        }
        // TODO: remove
        if (project.gradle.startParameter.taskNames.contains("bundleLocalDependencies")) {
            mavenLocal()
        } else {
            maven(url = uri("${project.rootDir}/mvn")) {
                name = "bundled local"
            }
        }
    }
}

// heroku stage
val stage = tasks.create("stage") {
    // TODO: add :backend:shadowJar
    // TODO: update jar path
//    dependsOn("clean")
    dependsOn(":backend:shadowJar")
    doLast {
        logger.lifecycle("jar was compiled")
    }
}

// debugging
System.getenv().forEach { (key, value) ->
    logger.info("$key : $value")
}