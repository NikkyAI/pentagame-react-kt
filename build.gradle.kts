plugins {
    kotlin("multiplatform") version Kotlin.version
    id("kotlinx-serialization") version Kotlin.version
    id("de.fayard.refreshVersions") // version "0.8.6"
    `maven-publish`
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
        maven(url = "https://dl.bintray.com/nwillc/maven")
//        mavenLocal()
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
    dependsOn("clean")
    dependsOn(":backend:flywayMigrate")
    dependsOn(":backend:flywayValidate")
    dependsOn(":backend:shadowJar")

    doLast {
        logger.lifecycle("jar was compiled")
    }
}

// debugging
System.getenv().forEach { (key, value) ->
    logger.info("$key : $value")
}

tasks.register<DefaultTask>("hello") {
    description = "Hello World"
    group = "help"
    doLast {
        logger.lifecycle("Hello World")
    }
}

kotlin {
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
    }
}