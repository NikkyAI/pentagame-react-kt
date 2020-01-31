plugins {
    kotlin("multiplatform") version Jetbrains.Kotlin.version// apply false
    id("kotlinx-serialization") version Jetbrains.Kotlin.version// apply false
    `build-scan`
    `maven-publish`
//    id("org.jetbrains.kotlin.frontend") version "0.0.45" apply false
}

allprojects {
    group = "moe.nikky.penta"

    // TODO: remove
    repositories {
        maven(url="https://dl.bintray.com/kotlin/kotlin-eap")
        mavenCentral()
        jcenter()
        maven(url = "https://dl.bintray.com/kotlin/kotlinx") {
            name = "kotlinx"
        }
        maven("https://dl.bintray.com/kotlin/kotlin-js-wrappers")
        maven(url = "https://dl.bintray.com/kotlin/ktor") {
            name = "ktor"
        }
        maven(url = "https://dl.bintray.com/data2viz/data2viz/") {
            name = "d2v"
        }
        maven(url = "https://dl.bintray.com/korlibs/korlibs/") {
            name = "korlibs"
        }
//        mavenLocal()
//        // TODO: remove
//        if (project.gradle.startParameter.taskNames.contains("bundleLocalDependencies")) {
//            mavenLocal()
//        } else {
//            maven(url = uri("${project.rootDir}/mvn")) {
//                name = "bundled local"
//            }
//        }
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

buildScan {
    termsOfServiceAgree = "yes"
    publishAlwaysIf(true)
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
}

tasks.register<DefaultTask>("hello") {
    description = "Hello World"
    group = "help"
    doLast {
        logger.lifecycle("Hello World")
    }
}

kotlin {
    jvm()
    js {
        useCommonJs()
        browser {
            runTask {
                sourceMaps = true
            }
            webpackTask {
                sourceMaps = true
            }
        }
        compilations.all {
            kotlinOptions {
                sourceMap = true
                metaInfo = true
                main = "call"
            }
        }
        mavenPublication { // Setup the publication for the target
//            artifactId = "ksvg-js"
            // Add a docs JAR artifact (it should be a custom task):
//            artifact(javadocJar)
        }
    }
}