import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompile
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

plugins {
    kotlin("multiplatform")
    id("kotlinx-serialization")
}

val genCommonSrcKt = buildDir.resolve("gen-src/commonMain/kotlin").apply { mkdirs() }
val genBackendResource = buildDir.resolve("gen-src/backendMain/resources").apply { mkdirs() }

val releaseTime = System.getenv("HEROKU_RELEASE_CREATED_AT") ?: run {
    val now = LocalDateTime.now(ZoneOffset.UTC)
    DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(now)
}

val gitCommitHash = System.getenv("SOURCE_VERSION") ?: captureExec("git", "rev-parse", "HEAD").trim()

val generateConstantsTask = tasks.create("generateConstants") {
    doLast {
        logger.lifecycle("generating constants")
        generateConstants(genCommonSrcKt, "penta", "Constants") {
            field("VERSION") value "0.0.1"
            field("GIT_HASH") value gitCommitHash
            field("RELEASE_TIME") value releaseTime
        }
    }
}
tasks.withType(AbstractKotlinCompile::class.java).all {
    logger.info("registered generating constants to $this")
    dependsOn(generateConstantsTask)
}

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

repositories {
//    mavenLocal()
//    jcenter()
    maven("https://dl.bintray.com/kotlin/kotlin-js-wrappers")
//    maven("https://dl.bintray.com/kotlin/kotlinx")
//    mavenCentral()
}

kotlin {
    jvm()
    js {
        nodejs()
    }

    /* Targets configuration omitted.
    *  To find out how to configure the targets, please follow the link:
    *  https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#setting-up-targets */

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:${Serialization.version}")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:${Coroutines.version}")

                api(Data2Viz.common_dep) {
                    exclude(mapOf("group" to Data2Viz.group, "module" to "geojson-common"))
                    exclude(mapOf("group" to Data2Viz.group, "module" to "d2v-geo-common"))
                }

                // logging
                api("io.github.microutils:kotlin-logging-common:${KotlinLogging.version}")

                // Redux
                api("org.reduxkotlin:redux-kotlin:0.2.9")
                api("org.reduxkotlin:redux-kotlin-reselect-metadata:0.2.9")

            }

            kotlin.srcDirs(genCommonSrcKt.path)
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:${Serialization.version}")
            }
        }

        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                api(kotlin("stdlib-jdk8"))
                // KTOR
                api(ktor("server-core", Ktor.version))
                api(ktor("server-cio", Ktor.version))
                api(ktor("websockets", Ktor.version))
                api(ktor("jackson", Ktor.version))

                // required because data2viz is referenced in common code
                // TODO: improve situation
                api(Data2Viz.jfx_dep) {
                    exclude(mapOf("group" to Data2Viz.group, "module" to "geojson-jvm"))
                    exclude(mapOf("group" to Data2Viz.group, "module" to "d2v-geo-jvm"))
                }

                // serialization
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime:${Serialization.version}")

                // Jackson
                api("com.fasterxml.jackson.core:jackson-databind:2.9.5")
                api("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.5")

                // logging
                api("ch.qos.logback:logback-classic:${Logback.version}")
                api("io.github.microutils:kotlin-logging:${KotlinLogging.version}")

                // redux
                api("org.reduxkotlin:redux-kotlin-jvm:0.2.9")
                api("org.reduxkotlin:redux-kotlin-reselect-jvm:0.2.9")

                // mongodb
//                    implementation("org.litote.kmongo:kmongo-serialization:3.11.1")
                api("org.litote.kmongo:kmongo-coroutine-serialization:3.11.1")
                api("org.litote.kmongo:kmongo-id:3.11.1")
            }
            kotlin.srcDirs(genBackendResource.path)
        }

        jvm().compilations["test"].defaultSourceSet {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:${Serialization.version}")
            }
        }

        val commonClient by creating {
            dependsOn(commonMain)
            dependencies {
                api(kotlin("stdlib-common"))
                api(Data2Viz.common_dep) {
                    exclude(mapOf("group" to Data2Viz.group, "module" to "geojson-common"))
                    exclude(mapOf("group" to Data2Viz.group, "module" to "d2v-geo-common"))
                }
                api(org.gradle.kotlin.dsl.ktor("client-core"))
                api(org.gradle.kotlin.dsl.ktor("client-json"))

//                api(ktor("client-websocket"))
            }
        }

        js().compilations["main"].defaultSourceSet {
            dependsOn(commonClient)
            dependencies {
                api(kotlin("stdlib-js"))

                api(Data2Viz.js_dep) {
                    exclude(mapOf("group" to Data2Viz.group, "module" to "geojson-js"))
                    exclude(mapOf("group" to Data2Viz.group, "module" to "d2v-geo-js"))
                }

                // coroutines
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:${Coroutines.version}")

                // serialization
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:${Serialization.version}")

                // logging
                api("io.github.microutils:kotlin-logging-js:${KotlinLogging.version}")

                // redux
                api("org.reduxkotlin:redux-kotlin-js:0.2.9")
//                api("org.reduxkotlin:redux-kotlin-reselect-js:0.2.9")

                // ktor client
                api(ktor("client-core-js"))
                api(ktor("client-json-js"))
                api(ktor("client-serialization-js"))

                api(npm("react", "^16.9.0"))
                api(npm("react-dom", "^16.9.0"))
//                api(npm("react-router-dom"))
                api(npm("styled-components", "^4.4.1"))
                api(npm("inline-style-prefixer", "^5.1.0"))
                api(npm("core-js", "^3.4.7"))
                api(npm("css-in-js-utils", "^3.0.2"))
                api(npm("redux", "^4.0.0"))
                api(npm("react-redux", "^5.0.7"))
//                api(npm("@types/redux-logger"))

                val kotlinWrappersVersion = "pre.89-kotlin-1.3.60"
                api("org.jetbrains:kotlin-react:16.9.0-${kotlinWrappersVersion}")
                api("org.jetbrains:kotlin-react-dom:16.9.0-${kotlinWrappersVersion}")
//                api("org.jetbrains:kotlin-react-router-dom:4.3.1-${kotlinWrappersVersion}")
                api("org.jetbrains:kotlin-css:1.0.0-${kotlinWrappersVersion}")
                api("org.jetbrains:kotlin-css-js:1.0.0-${kotlinWrappersVersion}")
                api("org.jetbrains:kotlin-styled:1.0.0-${kotlinWrappersVersion}")

                api("org.jetbrains:kotlin-redux:4.0.0-${kotlinWrappersVersion}")
                api("org.jetbrains:kotlin-react-redux:5.0.7-${kotlinWrappersVersion}")
            }
        }

        js().compilations["test"].defaultSourceSet {
            dependencies {
                api(kotlin("test-js"))
                implementation(kotlin("stdlib-js"))
            }
        }
    }
}

