import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinOnlyTarget
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompile
import proguard.ClassSpecification
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import org.jetbrains.kotlin.gradle.frontend.webpack.WebPackExtension

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

    id("org.jetbrains.kotlin.frontend") version "0.0.45"
//    application
    `maven-publish`
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://dl.bintray.com/kotlin/kotlinx") {
        name = "kotlinx"
    }
    maven(url = "https://dl.bintray.com/kotlin/ktor") {
        name = "ktor"
    }
    maven(url = "https://kotlin.bintray.com/kotlin-js-wrappers")
    maven(url = "https://dl.bintray.com/data2viz/data2viz/") {
        name = "d2v"
    }
//    maven(url = "https://dl.bintray.com/lightningkite/com.lightningkite.krosslin") {
//        name = "lightningkite.krosslin"
//    }

//    maven(url = "https://www.jitpack.io") {
//        name = "jitpack"
//    }
    if (project.gradle.startParameter.taskNames.contains("bundleLocalDependencies")) {
        mavenLocal()
    } else {
        maven(url = uri("${projectDir}/mvn")) {
            name = "bundled local"
        }
    }
}

val genCommonSrcKt = project.rootDir.resolve("build/gen-src/commonMain/kotlin").apply { mkdirs() }
val genServerResource = project.rootDir.resolve("build/gen-src/serverMain/resources").apply { mkdirs() }

//version = "0.0.1"
group = "moe.nikky.penta"

// debugging
System.getenv().forEach { (key, value) ->
    logger.lifecycle("$key : $value")
}

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

fun localBin(binary: String) = buildDir.resolve("node_modules/.bin/$binary").path

kotlin {
    val server = jvm("server") // Creates a JVM target for the server
    val clientFX = jvm("clientFx") // Creates a JVM target for the client
    val clientJS = js("clientJs")  // JS target named "client-js"

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

//                    implementation("org.jetbrains:kotlin-react:+")
//                    implementation("org.jetbrains:kotlin-styled:+")
                }
                compilations["test"].defaultSourceSet {

                }
                compilations.all {
                    kotlinOptions {
                        sourceMap = true
                        metaInfo = true
                        moduleKind = "amd"
                        sourceMapEmbedSources = "always"
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
                    logger.lifecycle("dce task outputs: " + dceTask.outputs.files.joinToString { it.name })

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
                    from("src/clientJsMain/web")
                    into(file("build/html/"))
                }
                copy {
                    from(file("build/node_modules/requirejs/require.js"))
                    into(file("build/html/js/"))
                }
            }
        }

        val terserTask = tasks.create("${target.name}Terser") {
            outputs.upToDateWhen { false }
            dependsOn(copyJsTask)
            dependsOn("npm-install")
            group = "build"
            val dir = file("build/terser")
            val outputDir = dir.resolve("out/js/")
            outputs.dir(outputDir.parentFile)
            doLast {
                val inputFolder = file("build/kotlin-js-min/${target.name}/main/")

                outputDir.deleteRecursively()
                outputDir.mkdirs()
                inputFolder.listFiles { file, name ->
                    !name.endsWith(".meta.js") && name.endsWith(".js")
                }!!.forEach { file ->
                    exec {
                        val f = file.name
                        commandLine(
                            localBin("terser"),
                            "$file",
                            "--source-map",
                            "content='$file.map',url='$f.map'",
                            "-o", outputDir.resolve(f).path
                        )
                        logger.lifecycle(commandLine.joinToString(" "))
                    }
                }
                copy {
                    from(file("node_modules/requirejs/require.js"))
                    into(outputDir)
                }
                copy {
                    from(outputDir.parentFile)
                    from("src/clientJsMain/web")
                    into(dir.resolve("html"))
                }
            }
        }

        val bundleTask = tasks.create("${target.name}Bundle") {
            outputs.upToDateWhen { false }
            dependsOn(copyJsTask)
            dependsOn("npm-install")
            group = "build"
            val dir = file("build/bundle")
            val outputDir = dir.resolve("out/js/")
            outputs.dir(outputDir.parentFile)
            doLast {
                val inputFolder = dir.resolve("input/")
                inputFolder.deleteRecursively()
                file("build/kotlin-js-min/${target.name}/main/").copyRecursively(inputFolder)
                file("build/node_modules/almond/almond.js").copyTo(inputFolder.resolve("almond.js"), true)

                outputDir.deleteRecursively()
                outputDir.mkdirs()
                exec {
                    workingDir(inputFolder)
                    commandLine(
                        localBin("r.js"),
                        "-o", "baseUrl=.", "name=almond.js",
                        "include=penta,almond",
                        "insertRequire=penta",
                        "out=${outputDir.resolve("bundle.js")}",
                        "wrap=true"
                    )
                    logger.lifecycle(commandLine.joinToString(" "))
                }

                copy {
                    from(outputDir)
                    from("src/clientJsMain/web")
                    into(dir)
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

//application {
//    mainClassName = "penta.app.PentaApp"
//}

val shadowJar = tasks.getByName<ShadowJar>("shadowJar") {
    archiveClassifier.set("client")

    group = "shadow"

    manifest {
        attributes(mapOf("Main-Class" to "penta.app.PentaApp"))
    }

    val target = kotlin.targets.getByName("clientFx") as KotlinOnlyTarget<KotlinJvmCompilation>
    from(target.compilations.getByName("main").output)
    val runtimeClasspath = target.compilations.getByName("main").runtimeDependencyFiles as Configuration
    configurations = listOf(runtimeClasspath)
    doLast {
        logger.lifecycle("task shadow jar")
    }

}

val terserTask = tasks.getByName("clientJsTerser")
val bundleTask = tasks.getByName("clientJsBundle")

val packageStaticForServer = tasks.create<Copy>("packageStaticForServer") {
    group = "build"
    dependsOn(terserTask)
    dependsOn(bundleTask)
//    dependsOn("clientJsCopyJsDev")
    val staticFolder = genServerResource.resolve("static").apply { mkdirs() }

    doFirst {
        staticFolder.deleteRecursively()
    }

//    from(project.buildDir.resolve("html"))
    from("src/clientJsMain/web")
    from(terserTask)
    from(bundleTask)
    into(staticFolder)
}

val shadowJarServer = tasks.create<ShadowJar>("shadowJarServer") {
    archiveClassifier.set("server")

    dependsOn(packageStaticForServer)

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

//val run = tasks.getByName<JavaExec>("run") {
//    group = "application"
//    dependsOn(shadowJar)
//
//    classpath(shadowJar.archiveFile)
//
//    workingDir = file("run").apply {
//        mkdirs()
//    }
//}

val runServer = tasks.create<JavaExec>("runServer") {
    group = "application"

    main = "io.ktor.server.cio.EngineMain"

    dependsOn(shadowJarServer)

    classpath(shadowJarServer.archiveFile)

    workingDir = file("run").apply {
        mkdirs()
    }
}

tasks.withType(JavaExec::class.java).all {

}

val bundleLocalDependencies = tasks.create("bundleLocalDependencies") {
    group = "build setup"
    val mavenLocal = File(System.getProperty("user.home")).resolve(".m2").resolve("repository")
    val mvnFolder = project.rootDir.resolve("mvn")
    doLast {
        mvnFolder.deleteRecursively()
        val configurations = kotlin.targets.flatMap { target: KotlinTarget ->
            logger.lifecycle("target: $target")
            target.compilations.flatMap { compilation ->
                logger.lifecycle("  compilation: $compilation ${compilation::class}")
                logger.lifecycle("   compileDependencyFiles: ${compilation.compileDependencyFiles}")

                compilation.relatedConfigurationNames.map { configurationName ->
                    configurations.getByName(configurationName)
                }
            }
        }
        configurations.forEach { configuration ->
            try {
                logger.lifecycle("configuration: $configuration")
                configuration.resolvedConfiguration.resolvedArtifacts.filter { artifact ->
                    // check if dependency is in mavenLocal()
                    artifact.file.startsWith(mavenLocal)
                }/*.filter { artifact ->
                    // check if dependency is matching
                    when(artifact.moduleVersion.id.group) {
                        "com.lightningkite" -> true
                        else -> false
                    }
                }*/.forEach { artifact ->
                    logger.lifecycle("  artifact: $artifact")
                    logger.lifecycle("    id ${artifact.id.componentIdentifier}")
                    logger.lifecycle("    file ${artifact.file}")
                    logger.lifecycle("    classifier ${artifact.classifier}")
                    logger.lifecycle("    moduleVersion ${artifact.moduleVersion}")
                    logger.lifecycle("    name ${artifact.name}")
                    logger.lifecycle("    type ${artifact.type}")

                    val groupFolder = artifact.file.parentFile.parentFile
                    val relative = groupFolder.relativeTo(mavenLocal)
                    mavenLocal.resolve(relative).copyRecursively(mvnFolder.resolve(relative), true)
                }
            } catch (e: IllegalStateException) {
                logger.info(e.message)
            }
        }
    }
}

val stage = tasks.create("stage") {
    dependsOn("clean")
    dependsOn(shadowJarServer)
    doLast {
        logger.lifecycle("jar was compiled")
    }
}

val node_version = "11.10.0"

kotlinFrontend {
    downloadNodeJsVersion = node_version
    sourceMaps = true

    npm {
        devDependency("webpack", "4.29.5")
        devDependency("npm", "latest")

        devDependency("react-scripts-kotlin", "2.1.2")
        devDependency("@jetbrains/kotlin-webpack-plugin", "latest")
        // compression and optimizations
        devDependency("requirejs", "^2.3.6")
        devDependency("almond", "^0.3.3")
        devDependency("terser", "^4.3.9")
    }
    bundle("webpack", delegateClosureOf<WebPackExtension>{
//        bundleName = "main"
        publicPath = "/"
        port = 8088
        stats = "errors-only"
    })
}

//val npmWorkingDir = buildDir.resolve("npm_working_dir")
//mapOf(
//    "start" to "react-scripts-kotlin start",
//    "build" to "react-scripts-kotlin build",
//    "eject" to "react-scripts-kotlin eject",
//    "gen-idea-libs" to "react-scripts-kotlin gen-idea-libs",
//    "get-types" to "react-scripts-kotlin get-types --dest=src/types",
//    "postinstall" to "npm run gen-idea-libs"
//).forEach { (id, command) ->
//    tasks.create("npm-run-$id") {
//        group ="react"
//        doLast {
//            exec {
//                workingDir(buildDir)
//                val a = command.split(" ")
//                commandLine(
//                    localBin(a.first()),
//                    *(a.drop(1).toTypedArray())
//                )
//                logger.lifecycle("exec: ${this.commandLine}")
//            }
//        }
//    }
//}