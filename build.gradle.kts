import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.canvas
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.script
import kotlinx.html.stream.appendHTML
import kotlinx.html.style
import kotlinx.html.unsafe
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinOnlyTarget
import proguard.ClassSpecification

buildscript {
    dependencies {
        classpath("net.sf.proguard:proguard-gradle:6.0.1")
    }
}

plugins {
    kotlin("multiplatform") version Jetbrains.Kotlin.version
    id("kotlinx-serialization") version Jetbrains.Kotlin.version

    // jvm
    id("com.github.johnrengelman.shadow") version "5.0.0"
    // js
    id("kotlin-dce-js") version Jetbrains.Kotlin.version
//    id("org.jetbrains.kotlin.frontend") version "0.0.45"
    application
    `maven-publish`
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
    maven(url = "https://dl.bintray.com/kotlin/kotlinx") {
        name = "kotlinx"
    }
    maven(url = "https://dl.bintray.com/kotlin/ktor") {
        name = "ktor"
    }
//    maven(url = "https://dl.bintray.com/data2viz/data2viz/") {
//        name = "d2v"
//    }
    maven(url = "https://www.jitpack.io") {
        name = "jitpack"
    }
}

group = "moe.nikky.penta"

kotlin {
    val server = jvm("server") // Creates a JVM target with the default name "jvm"
    val clientFX = jvm("client-fx") // Creates a JVM target with the default name "jvm"
    val clientJS = js("client-js")  // JS target named "js"

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api(Data2Viz.common_dep) {
                    exclude(mapOf("group" to Data2Viz.group, "module" to "geojson-common"))
                    exclude(mapOf("group" to Data2Viz.group, "module" to "d2v-geo-common"))
                }
                // serialization
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:0.11.0")
            }
        }
//        val commonLogic by creating {
//            dependsOn(commonMain)
//            dependencies {
//                implementation(kotlin("stdlib-common"))
//
//            }
//        }
        val commonClient by creating {
            dependsOn(commonMain)
            dependencies {
                api(kotlin("stdlib-common"))
                api(Data2Viz.common_dep) {
                    exclude(mapOf("group" to Data2Viz.group, "module" to "geojson-common"))
                    exclude(mapOf("group" to Data2Viz.group, "module" to "d2v-geo-common"))
                }
                api(ktor("client-core"))
//                api(ktor("client-websocket"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        server.compilations["main"].defaultSourceSet {
            dependsOn(commonMain)
            dependencies {
                api(kotlin("stdlib-jdk8"))
                // KTOR
                implementation(ktor("server-core", Ktor.version))
                implementation(ktor("server-cio", Ktor.version))
                implementation(ktor("websockets", Ktor.version))

                // TODO: move data2viz only into commonClient
                implementation(Data2Viz.jfx_dep) {
                    exclude(mapOf("group" to Data2Viz.group, "module" to "geojson-jvm"))
                    exclude(mapOf("group" to Data2Viz.group, "module" to "d2v-geo-jvm"))
                }
            }
        }

        // Default source set for JVM-specific sources and dependencies:
//        val jvmMain = jvm()
        clientFX.apply {
            compilations["main"].defaultSourceSet {
                dependsOn(commonClient)
                dependencies {
                    implementation(kotlin("stdlib-jdk8"))
                    implementation(Data2Viz.jfx_dep) {
                        exclude(mapOf("group" to Data2Viz.group, "module" to "geojson-jvm"))
                        exclude(mapOf("group" to Data2Viz.group, "module" to "d2v-geo-jvm"))
                    }
                    implementation(TornadoFX.dep)
                    implementation(ktor("client-cio"))
//                    implementation(ktor("client-websockets"))
                }
            }
            // JVM-specific tests and their dependencies:
            compilations["test"].defaultSourceSet {
                dependencies {
                    implementation(kotlin("test-junit"))
                }
            }
        }

        clientJS.apply {
            compilations["main"].defaultSourceSet {
                dependsOn(commonClient)
                dependencies {
                    api(kotlin("stdlib-js"))
                    implementation(Data2Viz.js_dep) {
                        exclude(mapOf("group" to Data2Viz.group, "module" to "geojson-js"))
                        exclude(mapOf("group" to Data2Viz.group, "module" to "d2v-geo-js"))
                    }
                    implementation(ktor("client-core-js"))
//                    implementation(ktor("client-cio"))
//                    implementation(ktor("client-websocket"))
                }
                compilations["test"].defaultSourceSet {

                }
                compilations.all {
                    kotlinOptions {
                        sourceMap = true
                        moduleKind = "umd"
                        metaInfo = true
                    }
                }
            }
        }
    }
}


kotlin.sourceSets.all {
    languageSettings.progressiveMode = true
}

kotlin.targets.forEach { target: KotlinTarget ->
    task<DefaultTask>("depsize-${target.targetName}") {
        group = "help"
        description = "prints dependency sizes"
        doLast {
            val formatStr = "%,10.2f"
            val configuration = target.compilations.getByName("main").compileDependencyFiles as Configuration
            val size = configuration.resolve()
                .map { it.length() / (1024.0 * 1024.0) }.sum()

            val out = buildString {
                append("Total dependencies size:".padEnd(45))
                append("${String.format(formatStr, size)} Mb\n\n")
                configuration
                    .resolve()
                    .sortedWith(compareBy { -it.length() })
                    .forEach {
                        append(it.name.padEnd(45))
                        append("${String.format(formatStr, (it.length() / 1024.0))} kb\n")
                    }
            }
            println(out)
        }
    }
}

//base {
//    archivesBaseName = ""
//}

application {
    mainClassName = "penta.app.PentaAppKt"
}

val shadowJar = tasks.getByName<ShadowJar>("shadowJar") {
    archiveClassifier.set("client")

    group = "shadow"

    val target = kotlin.targets.getByName("client-fx") as KotlinOnlyTarget<KotlinJvmCompilation>
    from(target.compilations.getByName("main").output)
    val runtimeClasspath = target.compilations.getByName("main").runtimeDependencyFiles as Configuration
    configurations = listOf(runtimeClasspath)
    logger.lifecycle("task shadow jar")
}

val shadowJarServer = tasks.create<ShadowJar>("shadowJarServer") {
    archiveClassifier.set("server")

    group = "shadow"

    manifest {
        attributes(mapOf("Main-Class" to "io.ktor.server.cio.EngineMain"))
    }

    val target = kotlin.targets.getByName("server") as KotlinOnlyTarget<KotlinJvmCompilation>
    from(target.compilations.getByName("main").output)
    val runtimeClasspath = target.compilations.getByName("main").runtimeDependencyFiles as Configuration
    configurations = listOf(runtimeClasspath)
    logger.lifecycle("task shadow jar")
}

val minimizedServerJar = tasks.create<proguard.gradle.ProGuardTask>("minimizedServerJar") {
    dependsOn(shadowJarServer)
    outputs.upToDateWhen { false }
    group = "proguard"

    injars(shadowJarServer.archiveFile)
    outjars(shadowJarServer.archiveFile.get().asFile.let {
        it.parentFile.resolve(it.nameWithoutExtension + "-min.jar")
    })
    libraryjars(System.getProperty("java.home") + "/lib/rt.jar")
    libraryjars(System.getProperty("java.home") + "/lib/jfxrt.jar")
    printmapping("build/libs/penta-server.map")
    ignorewarnings()
    dontobfuscate()
    dontoptimize()
    dontwarn()

    val keepClasses = listOf(
        "io.ktor.server.cio.EngineMain", // The EngineMain you use, cio in this case.
        "kotlin.reflect.jvm.internal.**",
        "kotlin.text.RegexOption",
        "org.slf4j.impl.StaticLoggerBinder",
        "penta.**"
    )

    for (keepClass in keepClasses) {
        keep(
            mutableMapOf(
                "access" to "public",
                "name" to keepClass
            ),
            closureOf<ClassSpecification> {
                method(mapOf("access" to "public"))
                method(mapOf("access" to "private"))
            }
        )
    }
}
val minimizedClientJar = tasks.create<proguard.gradle.ProGuardTask>("minimizedClientJar") {
    dependsOn(shadowJar)
    outputs.upToDateWhen { false }
    group = "proguard"

    injars(shadowJar.archiveFile)
    outjars(shadowJar.archiveFile.get().asFile.let {
        it.parentFile.resolve(it.nameWithoutExtension + "-min.jar")
    })
    libraryjars(System.getProperty("java.home") + "/lib/rt.jar")
    printmapping("build/libs/penta-client.map")
    ignorewarnings()
    dontobfuscate()
    dontoptimize()
    dontwarn()
    dontskipnonpubliclibraryclassmembers()

    val keepClasses = listOf(
        "kotlin.reflect.jvm.internal.**",
        "kotlin.text.RegexOption",
        "org.slf4j.impl.StaticLoggerBinder",
        "penta.**",
//        "tornadofx.**",
        "org.osgi.**",
        "org.apache.**",
        "watchDog.main"
    )

    for (keepClass in keepClasses) {
        keep(
            mutableMapOf(
                "access" to "public",
                "name" to keepClass
            ),
            closureOf<ClassSpecification> {
                method(mapOf("access" to "public"))
                method(mapOf("access" to "private"))
            }
        )
    }
}

val run = tasks.getByName<JavaExec>("run") {
    group = "application"
    dependsOn(shadowJar)

    classpath(shadowJar.archiveFile)

    workingDir = file("run").apply {
        mkdirs()
    }
}

val runServer = tasks.create<JavaExec>("runServer") {
    group = "application"

    this.main = "io.ktor.server.cio.EngineMain"

    dependsOn(shadowJarServer)

    classpath(shadowJarServer.archiveFile)

    workingDir = file("run").apply {
        mkdirs()
    }
}

// JAVASCRIPT

val runDce = tasks.getByName("runDceClient-jsKotlin")

val packageJs = tasks.create("packageJs") {
    group = "build"
    dependsOn(runDce)

    doLast {
        val jsInput = buildDir
            .resolve("kotlin-js-min")
            .resolve("client-js")
            .resolve("main")

        val htmlOutput = buildDir
            .resolve("html")

        htmlOutput.deleteRecursively()
        htmlOutput.mkdir()
        val jsOutput = htmlOutput.resolve("js").apply {
            mkdir()
        }

        logger.lifecycle("input directory: $jsInput")

        jsInput.listFiles().forEach { file ->
            file.copyTo(jsOutput.resolve(file.name))
        }

        val htmlText = buildString {
            appendln("<!DOCTYPE html>")
            appendHTML().html {
                head {
                    script(src = "https://cdnjs.cloudflare.com/ajax/libs/require.js/2.3.5/require.min.js") {
                        this.attributes["data-main"] = "js/penta.js"
                    }
                    style {
                        unsafe {
                            this.raw(
                                """
                                |
                                |    html, body {
                                |      width: 100%;
                                |      height: 100%;
                                |      margin: 0px;
                                |      border: 0;
                                |      overflow: hidden; /*  Disable scrollbars */
                                |      display: block;  /* No floating content on sides */
                                |    }
                                |
                            """.trimMargin()
                            )
                        }
                    }
                }
                body {
                    canvas {
                        id = "viz"
                    }
                }
            }
            appendln()
        }
        htmlOutput.resolve("index.html").writeText(htmlText)
    }
}

tasks.withType(JavaExec::class.java).all {

}
