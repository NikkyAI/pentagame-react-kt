import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinOnlyTarget
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompile
import proguard.ClassSpecification
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import org.gradle.kotlin.dsl.withType

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
    maven(url = uri("${projectDir}/mvn")) {
        name = "bundled local"
    }
//    mavenLocal()
    mavenCentral()
    jcenter()
    maven(url = "https://dl.bintray.com/kotlin/kotlinx") {
        name = "kotlinx"
    }
    maven(url = "https://dl.bintray.com/kotlin/ktor") {
        name = "ktor"
    }
    maven(url = "https://dl.bintray.com/data2viz/data2viz/") {
        name = "d2v"
    }
//    maven(url = "https://dl.bintray.com/lightningkite/com.lightningkite.krosslin") {
//        name = "lightningkite.krosslin"
//    }

//    maven(url = "https://www.jitpack.io") {
//        name = "jitpack"
//    }
}

val genCommonSrcKt = project.rootDir.resolve("build/gen-src/commonMain/kotlin").apply { mkdirs() }
val genServerResource = project.rootDir.resolve("build/gen-src/serverMain/resources").apply { mkdirs() }

//version = "0.0.1"
group = "moe.nikky.penta"

System.getenv().forEach { (key, value) ->
    logger.lifecycle("$key : $value")
}

val releaseTime = System.getenv("HEROKU_RELEASE_CREATED_AT") ?:run {
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


kotlin {
    val server = jvm("server") // Creates a JVM target for the server
    val clientFX = jvm("client-fx") // Creates a JVM target for the client
    val clientJS = js("client-js")  // JS target named "client-js"

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api(Data2Viz.common_dep) {
                    exclude(mapOf("group" to Data2Viz.group, "module" to "geojson-common"))
                    exclude(mapOf("group" to Data2Viz.group, "module" to "d2v-geo-common"))
                }
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:${Coroutines.version}")
                // serialization
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:${Serialization.version}")

                api("io.github.microutils:kotlin-logging-common:${KotlinLogging.version}")

                api("com.lightningkite:kommon-metadata:${Kommon.version}")
                api("com.lightningkite:reacktive-metadata:${Reacktive.version}")
            }

            kotlin.srcDirs(genCommonSrcKt.path)
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
                api(ktor("client-json"))
                api("com.lightningkite:kommon-metadata:${Kommon.version}")
                api("com.lightningkite:reacktive-metadata:${Reacktive.version}")
                api("com.lightningkite:recktangle-metadata:${Recktangle.version}")
//                api("com.lightningkite:lokalize-metadata:${Lokalize.version}")
                api("com.lightningkite:koolui-metadata:${KoolUI.version}")

//                api(ktor("client-websocket"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        server.apply {
            compilations["main"].defaultSourceSet {
                dependsOn(commonMain)
                dependencies {
                    api(kotlin("stdlib-jdk8"))
                    // KTOR
                    implementation(ktor("server-core", Ktor.version))
                    implementation(ktor("server-cio", Ktor.version))
                    implementation(ktor("websockets", Ktor.version))
                    implementation(ktor("jackson", Ktor.version))

                    // serialization
                    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:${Serialization.version}")

                    // Jackson
                    implementation("com.fasterxml.jackson.core:jackson-databind:2.9.5")
                    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.5")

                    // logging
                    implementation("ch.qos.logback:logback-classic:${Logback.version}")
                    implementation("io.github.microutils:kotlin-logging:${KotlinLogging.version}")

                    // TODO: move data2viz only into commonClient
                    implementation(Data2Viz.jfx_dep) {
                        exclude(mapOf("group" to Data2Viz.group, "module" to "geojson-jvm"))
                        exclude(mapOf("group" to Data2Viz.group, "module" to "d2v-geo-jvm"))
                    }
                    // krosslin
                    implementation("com.lightningkite:kommon-jvm:${Kommon.version}")
                    implementation("com.lightningkite:reacktive-jvm:${Reacktive.version}")

                    // mongodb
//                    implementation("org.litote.kmongo:kmongo-serialization:3.11.1")
                    implementation("org.litote.kmongo:kmongo-coroutine-serialization:3.11.1")
                    implementation("org.litote.kmongo:kmongo-id:3.11.1")
                }

                resources.srcDirs(genServerResource.path)
            }
//            compilations["main"].allKotlinSourceSets.forEach {
//                logger.lifecycle("sourceSet: $it")
//                it.resources
//            }
            compilations.all {
                kotlinOptions {
                    jvmTarget = "1.8"
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

                    // coroutines
                    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Coroutines.version}")
                    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:${Coroutines.version}")

                    // serialization
                    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:${Serialization.version}")

                    // logging
                    implementation("ch.qos.logback:logback-classic:${Logback.version}")
                    implementation("io.github.microutils:kotlin-logging:${KotlinLogging.version}")

                    // ktor client
                    implementation(ktor("client-cio"))
                    implementation(ktor("client-json-jvm"))
                    implementation(ktor("client-serialization-jvm"))
//                    implementation(ktor("client-websockets"))

                    // krosslin
                    implementation("com.lightningkite:kommon-jvm:${Kommon.version}")
                    implementation("com.lightningkite:reacktive-jvm:${Reacktive.version}")
                    implementation("com.lightningkite:recktangle-jvm:${Recktangle.version}")
//                    implementation("com.lightningkite:lokalize-jvm:${Lokalize.version}")
                    implementation("com.lightningkite:koolui-javafx:${KoolUI.version}")

                }
            }
            // JVM-specific tests and their dependencies:
            compilations["test"].defaultSourceSet {
                dependencies {
                    implementation(kotlin("test-junit5"))
                }
            }
            compilations.all {
                kotlinOptions {
                    jvmTarget = "1.8"
                }
            }
//                test {
//                    useJUnitPlatform()
//                }
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

                    // coroutines
                    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:${Coroutines.version}")

                    // serialization
                    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:${Serialization.version}")

                    // logging
                    implementation("io.github.microutils:kotlin-logging-js:${KotlinLogging.version}")

                    // ktor client
                    implementation(ktor("client-core-js"))
                    implementation(ktor("client-json-js"))
                    implementation(ktor("client-serialization-js"))

                    // krosslin
                    implementation("com.lightningkite:kommon-js:${Kommon.version}")
                    implementation("com.lightningkite:reacktive-js:${Reacktive.version}")
                    implementation("com.lightningkite:recktangle-js:${Recktangle.version}")
//                    implementation("com.lightningkite:lokalize-js:${Lokalize.version}")
                    implementation("com.lightningkite:koolui-js:${KoolUI.version}")
                }
                compilations["test"].defaultSourceSet {

                }
                compilations.all {
                    kotlinOptions {
                        sourceMap = true
                        metaInfo = true
                        moduleKind = "amd"
                        sourceMapEmbedSources = "always"
//                        outputFile = "penta.js"
                    }
                }
            }
        }
        val target = clientJS

        fun getDependencies(): FileCollection {
            val dependencies = ArrayList<File>()
            try {
                for (configName in target.compilations.findByName("main")!!.relatedConfigurationNames) {
                    try {
                        val config = project.configurations.getByName(configName)
                        for (file in config) {
                            dependencies.add(file)
                        }
                        println("Successfully read config ${configName}")
                    } catch (e: Throwable) {
                        /*squish*/
                        println("Failed read config ${configName}")
                    }
                }
            } catch (e: Throwable) {
                logger.error(e.message, e)
//                e.printStackTrace()
            }
            return files(dependencies)
        }

        fun File.jarFileToJsFiles(): FileCollection {
            return if (exists()) {
                zipTree(this).filter { it.extension == "js" || it.extension == "map" }
            } else {
                files()
            }
        }

        val jarTask = tasks.findByName("${target.name}Jar")!!
        val dceTask = tasks.findByName("runDce${target.name.capitalize()}Kotlin")

        val copyJsTask = tasks.create("${target.name}CopyJs") {
            outputs.upToDateWhen { false }
            //, Copy::class.java) { task ->
            group = "build"
            val targetFolder = file("build/kotlin-js/${target.name}/")
            if (dceTask != null) {
                dependsOn(dceTask)
                doLast {
                    logger.lifecycle("dce task outputs: "+ dceTask.outputs.files.joinToString { it.name })

                    copy {
                        into(targetFolder)
                        from(dceTask.outputs.files)
                    }
                }
            } else {
                dependsOn(jarTask)
                doLast {
                    targetFolder.deleteRecursively()
                    copy {
                        into(targetFolder)
                        jarTask.outputs.files
                            .filter { it.extension == "jar" }
                            .flatMap { it.jarFileToJsFiles() }
                            .forEach {
                                from(it)
                            }
                        from(getDependencies().flatMap { it.jarFileToJsFiles() })
                    }
                }
            }
        }
        val devJsTask = tasks.create("${target.name}CopyJsDev") {
            group = "build"
            val targetFolder = file("build/html/js/")
            dependsOn(jarTask)
            doLast {
                //                targetFolder.deleteRecursively()
                copy {
                    into(targetFolder)
                    jarTask.outputs.files
                        .filter { it.extension == "jar" }
                        .flatMap { it.jarFileToJsFiles() }
                        .forEach {
                            from(it)
                        }
                    from(getDependencies().flatMap { it.jarFileToJsFiles() })
                }
                copy {
                    from("src/client-jsMain/web")
                    into(file("build/html/"))
                }
            }
        }
        val installTerser = tasks.create("installTerser") {
            doLast {
                exec {
                    commandLine("npm", "install", "-g", "terser")
                    isIgnoreExitValue = true
                }
            }
        }

        val terseTask = tasks.create("${target.name}TerseJs") {
            dependsOn(installTerser)
            dependsOn(copyJsTask)
            group = "build"
            val outputDir = file("build/html/js")
            outputs.dir(outputDir)
            doLast {
                outputDir.deleteRecursively()
                outputDir.mkdirs()
                file("build/kotlin-js/${target.name}/").listFiles { file, name ->
                    !name.endsWith(".meta.js") && name.endsWith(".js")
                }!!.forEach { file ->
                    exec {
                        val f = file.name
                        commandLine(
                            "terser",
                            "$file",
                            "--source-map",
                            "content='$file.map',url='$f.map'",
                            "-o",
                            outputDir.resolve(f).path
                        )
                        logger.lifecycle(commandLine.joinToString(" "))
                    }
                }
                copy {
                    from("src/client-jsMain/web")
                    into(file("build/html/"))
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

//sourceSets.all {
//    logger.lifecycle("sourceSet: $this")
//}

//base {
//    archivesBaseName = ""
//}

// JVM

application {
    mainClassName = "penta.app.PentaApp"
}

val shadowJar = tasks.getByName<ShadowJar>("shadowJar") {
    archiveClassifier.set("client")

    group = "shadow"

    val target = kotlin.targets.getByName("client-fx") as KotlinOnlyTarget<KotlinJvmCompilation>
    from(target.compilations.getByName("main").output)
    val runtimeClasspath = target.compilations.getByName("main").runtimeDependencyFiles as Configuration
    configurations = listOf(runtimeClasspath)
    doLast {
        logger.lifecycle("task shadow jar")
    }

}

val packageStaticForServer = tasks.create<Copy>("packageStaticForServer") {
    group = "build"
    dependsOn("client-jsTerseJs")
//    dependsOn("client-jsCopyJsDev")
    val staticFolder = genServerResource.resolve("static").apply { mkdirs() }

    from(project.buildDir.resolve("html"))
    into(staticFolder)
    doLast {
        copy {
            from("src/client-jsMain/web")
            into(staticFolder)
        }
    }
}

val shadowJarServer = tasks.create<ShadowJar>("shadowJarServer") {
    archiveClassifier.set("server")

    dependsOn(packageStaticForServer)
//    include(project.rootDir.resolve("build/html").path)
//    include("*.jar")

    group = "shadow"

    manifest {
        attributes(mapOf("Main-Class" to "io.ktor.server.cio.EngineMain"))
    }

    val target = kotlin.targets.getByName("server") as KotlinOnlyTarget<KotlinJvmCompilation>
    from(target.compilations.getByName("main").output)
    val runtimeClasspath = target.compilations.getByName("main").runtimeDependencyFiles as Configuration
    configurations = listOf(runtimeClasspath)
    doLast {
        logger.lifecycle("task shadow jar")
    }
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

tasks.withType(JavaExec::class.java).all {

}

val bundleLocalDependencies = tasks.create("bundleLocalDependencies") {
    val mavenLocal = File(System.getProperty("user.home")).resolve(".m2").resolve("repository")
    val dependencies = listOf(
        File("com/lightningkite/")
    )
    val mvnFolder = project.rootDir.resolve("mvn")

    doLast {
        logger.lifecycle("mavenLocal: $mavenLocal")
        mvnFolder.deleteRecursively()
        mvnFolder.mkdirs()

        dependencies.forEach { relative ->
            mavenLocal.resolve(relative).copyRecursively(
                mvnFolder.resolve(relative)
            )
        }

    }
}
//val buildLocalDependencies = tasks.create("buildLocalDependencies") {
//    group = "build setup"
//
//    doLast {
//
//
//    }
//}

val stage = tasks.create("stage") {
    dependsOn("clean")
    dependsOn(shadowJarServer)
    doLast {
        logger.lifecycle("jar was compiled")
    }
}
