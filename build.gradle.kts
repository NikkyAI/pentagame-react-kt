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
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
        mavenCentral()
        jcenter()
        maven(url = "https://jcenter.bintray.com/")
        maven(url = "https://dl.bintray.com/kotlin/kotlinx")
        maven(url = "https://dl.bintray.com/kotlin/kotlin-js-wrappers")
        maven(url = "https://dl.bintray.com/kotlin/ktor")
        maven(url = "https://dl.bintray.com/data2viz/data2viz/")
        maven(url = "https://dl.bintray.com/korlibs/korlibs/")
        maven(url = "https://dl.bintray.com/kotlin/exposed")
        maven(url = "https://dl.bintray.com/kenjiohtsuka/m")
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
    val privateScript = rootDir.resolve("private.gradle.kts")
    if(privateScript.exists()) {
        apply(from = privateScript)
    } else {
        val DEV_JDBC_DATABASE_URL by extra("jdbc:postgresql://localhost:5432/pentagame?user=postgres")
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