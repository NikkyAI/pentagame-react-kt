import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinOnlyTarget
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

                api("com.lightningkite:kommon-metadata:${Kommon.version}")
                api("com.lightningkite:reacktive-metadata:${Reacktive.version}")
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

                // krosslin
                api("com.lightningkite:kommon-jvm:${Kommon.version}")
                api("com.lightningkite:reacktive-jvm:${Reacktive.version}")

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
                api("com.lightningkite:kommon-metadata:${Kommon.version}")
                api("com.lightningkite:reacktive-metadata:${Reacktive.version}")
                api("com.lightningkite:recktangle-metadata:${Recktangle.version}")
//                api("com.lightningkite:lokalize-metadata:${Lokalize.version}")
                api("com.lightningkite:koolui-metadata:${KoolUI.version}")

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

                // ktor client
                api(org.gradle.kotlin.dsl.ktor("client-core-js"))
                api(org.gradle.kotlin.dsl.ktor("client-json-js"))
                api(org.gradle.kotlin.dsl.ktor("client-serialization-js"))

                // krosslin
                api("com.lightningkite:kommon-js:${Kommon.version}")
                api("com.lightningkite:reacktive-js:${Reacktive.version}")
                api("com.lightningkite:recktangle-js:${Recktangle.version}")
//                    implementation("com.lightningkite:lokalize-js:${Lokalize.version}")
                api("com.lightningkite:koolui-js:${KoolUI.version}")

            }
        }

        js().compilations["test"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-js"))
            }
        }
    }
}

