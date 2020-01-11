import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

var ktorVersion = "1.2.6"
var logbackVersion = "1.2.3"


plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "5.0.0"
    application
}

val gen_resource = buildDir.resolve("gen-src/resources").apply { mkdirs() }

//sourceSets {
//    main {
//        resources.srcDirs += gen_resource
//    }
//}

repositories {
    jcenter()
    mavenCentral()
    maven(uri("https://dl.bintray.com/kotlin/ktor"))
    maven(uri("https://dl.bintray.com/kotlin/kotlinx"))
    maven(uri("https://dl.bintray.com/kotlin/kotlin-dev"))
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":shared"))

    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-html-builder:$ktorVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.13.0")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

val packageStatic = tasks.create("packageStatic") {
    group = "build"
    val frontend = project(":frontend")
    dependsOn(":frontend:processResources")
    dependsOn(":frontend:browserWebpack")

    outputs.upToDateWhen { false }
    outputs.dir(gen_resource)

    val staticFolder = gen_resource.resolve("static").apply { mkdirs() }

    doFirst {
        staticFolder.deleteRecursively()
        staticFolder.mkdirs()
    }



    // TODO: readd terser and require.js for dev
//    from(terserTask)
//    from(bundleTask)

    doLast {
        copy {
            from(frontend.tasks.get("processResources"))
            from(frontend.buildDir.resolve("processedResources/js/main"))
            into(staticFolder)
        }
        copy {
            from(frontend.buildDir.resolve("distributions"))
            into(staticFolder.resolve("js"))
        }
    }
}

application {
    mainClassName = "io.ktor.server.cio.EngineMain"
}

val shadowJar = tasks.getByName<ShadowJar>("shadowJar") {
    dependsOn(packageStatic)

    from(packageStatic)
}

task<DefaultTask>("depsize") {
    group = "help"
    description = "prints dependency sizes"
    doLast {
        val formatStr = "%,10.2f"
        val configuration = kotlin.target.compilations.getByName("main").compileDependencyFiles as Configuration
        val size = configuration.resolve()
            .map { it.length() / (1024.0 * 1024.0) }.sum()

        val out = buildString {
            append("Total dependencies size:".padEnd(55))
            append("${String.format(formatStr, size)} Mb\n\n")
            configuration
                .resolve()
                .sortedWith(compareBy { -it.length() })
                .forEach {
                    append(it.name.padEnd(55))
                    append("${String.format(formatStr, (it.length() / 1024.0))} kb\n")
                }
        }
        println(out)
    }
}

//val run = tasks.getByName<JavaExec>("run") {
//    dependsOn(shadowJar)
//}

//tasks.register<JavaExec>("run") {
//    dependsOn("jar")
//
//    main = "com.bdudelsack.fullstack.ApplicationKt"
//
//    classpath(configurations["runtimeClasspath"].resolvedConfiguration.files,jar.archiveFile.get().toString())
//    args = listOf()
//}
