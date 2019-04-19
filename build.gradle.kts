import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import kotlinx.html.body
import kotlinx.html.canvas
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

plugins {
    kotlin("multiplatform") version Jetbrains.Kotlin.version

    // jvm
    id("com.github.johnrengelman.shadow") version "5.0.0"
    // js
//    id("kotlin2js") version Jetbrains.Kotlin.version
    id("kotlin-dce-js") version Jetbrains.Kotlin.version
//    id("org.jetbrains.kotlin.frontend") version "0.0.45"
    application
}

//apply(plugin = "kotlin-dce-js")

repositories {
    mavenLocal()
    mavenCentral()
//    maven(url = "https://dl.bintray.com/data2viz/data2viz/") {
//        name = "d2v"
//    }
}

kotlin {
    jvm() // Creates a JVM target with the default name 'jvm'
    js()  // JS target named 'js'

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(Data2Viz.common_dep)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        // Default source set for JVM-specific sources and dependencies:
        val jvmmain = jvm()
        jvmmain.compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation(Data2Viz.jfx_dep)
                implementation("no.tornado:tornadofx:1.7.18")
            }
        }
        // JVM-specific tests and their dependencies:
        jvm().compilations["test"].defaultSourceSet {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }

        js().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(Data2Viz.js_dep)
            }
        }
        js().compilations.all {
            kotlinOptions {
                sourceMap = true
                moduleKind = "umd"
                metaInfo = true
            }
        }
        js().compilations["test"].defaultSourceSet {

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

val shadowJar = tasks.getByName<ShadowJar>("shadowJar") {
    archiveClassifier.set("")

    group = "shadow"
    val target = kotlin.targets.getByName("jvm") as KotlinOnlyTarget<KotlinJvmCompilation>
    from(target.compilations.getByName("main").output)
    val runtimeClasspath = target.compilations.getByName("main").runtimeDependencyFiles as Configuration
    configurations = listOf(runtimeClasspath)
    logger.lifecycle("task shadow jar")
}

application {
    mainClassName = "app.PentaAppKt"
}

val run = tasks.getByName<JavaExec>("run") {
    dependsOn(shadowJar)

    classpath(shadowJar.archiveFile)

    workingDir = file("run").apply {
        mkdirs()
    }
}

val runDceJsKotlin = tasks.getByName("runDceJsKotlin")

val packageJs = tasks.create("packageJs") {
    group = "build"
    dependsOn(runDceJsKotlin)

    doLast {
        val jsInput = buildDir
            .resolve("kotlin-js-min")
            .resolve("js")
            .resolve("main")

        val htmlOutput = buildDir
            .resolve("html")

        htmlOutput.deleteRecursively()
        htmlOutput.mkdir()
        val jsOutput = htmlOutput.resolve("js-min").apply {
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
                    script(src="https://cdnjs.cloudflare.com/ajax/libs/require.js/2.3.5/require.min.js") {
                        this.attributes["data-main"] = "js-min/penta.js"
                    }
                    style {
                        unsafe {
                            this.raw("""
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
                            """.trimMargin())
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
