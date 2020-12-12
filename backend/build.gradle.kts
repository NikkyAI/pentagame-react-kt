import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.io.ByteArrayOutputStream
import java.io.PrintStream

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
    id("com.squareup.sqldelight")
    application
//    id("org.flywaydb.flyway") version Flyway.version
//    id("de.fayard.dependencies")
}

val gen_resource = buildDir.resolve("gen-src/resources").apply { mkdirs() }

/*
val hasDevUrl = extra.has("DEV_JDBC_DATABASE_URL")
if (!hasDevUrl) logger.error("DEV_JDBC_DATABASE_URL not set")
val hasLiveUrl = extra.has("LIVE_JDBC_DATABASE_URL")
if (!hasLiveUrl) logger.error("LIVE_JDBC_DATABASE_URL not set")

val flywayUrl = System.getenv()["JDBC_DATABASE_URL"] ?: if (hasDevUrl) {
    val DEV_JDBC_DATABASE_URL: String by extra
    DEV_JDBC_DATABASE_URL
} else {
    null
}
if(flywayUrl != null) {
//    val jdbc = split_jdbc(flywayUrl)
    flyway {
        url = flywayUrl
        schemas = arrayOf("public")
        baselineVersion = "0"
    }
}
if (hasDevUrl) {
//    tasks.register<DefaultTask>("resetDatabase") {
//        group = "database"
//
//        doFirst {
//
//        }
//        this.finalizedBy("")
//
//    }
}
if (hasDevUrl && hasLiveUrl) {
    val DEV_JDBC_DATABASE_URL: String by extra
    val LIVE_JDBC_DATABASE_URL: String by extra

    tasks.register<DefaultTask>("createMigration") {
        group = "database"

        dependsOn("testClasses")

        val dumps = buildDir.resolve("dumps")

        doFirst {
            dumps.mkdirs()
            val livePath = dumps.resolve("live_schema.sql").path
            val devPath = dumps.resolve("dev_schema.sql").path

            pg_dump(
                connectionString = LIVE_JDBC_DATABASE_URL,
                target = livePath,
                extraArgs = arrayOf(
                    "--create",
                    "--no-owner",
                    "--no-acl",
                    "--exclude-table", "flyway_schema_history"
                    /*, "--schema-only"*/)
            )

            // drop and regenerate dev database
            javaexec {
                main = "util.ResetDB"
                classpath = sourceSets.test.get().runtimeClasspath
                environment("JDBC_DATABASE_URL", DEV_JDBC_DATABASE_URL)
            }

            pg_dump(
                connectionString = DEV_JDBC_DATABASE_URL,
                target = devPath,
                extraArgs = arrayOf(
                    "--create",
                    "--no-owner",
                    "--no-acl",
                    "--exclude-table", "flyway_schema_history"
                )
            )

            val old = System.out
            val migrationStatements = ByteArrayOutputStream().use { os ->
                System.setOut(PrintStream(os))

                cz.startnet.utils.pgdiff.Main.main(
                    arrayOf(
                        "--ignore-start-with",
                        livePath,
                        devPath
                    )
                )
                os.toString()
            }
            System.setOut(old)

//        val migrationStatements = ByteArrayOutputStream().use { os ->
//            javaexec {
//                main = "cz.startnet.utils.pgdiff.Main"
//                classpath = files("apgdiff-2.4.jar")
//                args = listOf("--ignore-start-with", dumps.resolve("v0.sql").path, dumps.resolve("v1.sql").path)
//                standardOutput = os
//            }
//            os.toString()
//        }

            logger.lifecycle("migration statements \n$migrationStatements")

            file("src/main/resources/db/migration/next.sql")
                .writeText(migrationStatements)
            // TODO: write statements to file
        }
    }
}
*/

dependencies {
    implementation(kotlin("stdlib-jdk8", "_"))

    implementation(project(":shared"))

    implementation("io.github.microutils:kotlin-logging:_")

    implementation("io.rsocket.kotlin:rsocket-core:_")
    implementation("io.rsocket.kotlin:rsocket-transport-ktor:_")
    implementation("io.rsocket.kotlin:rsocket-transport-ktor-server:_")

    implementation("io.ktor:ktor-server-netty:_")
    implementation("io.ktor:ktor-html-builder:_")
    implementation("io.ktor:ktor-serialization:_")
    implementation("ch.qos.logback:logback-classic:_")

    implementation("org.koin:koin-ktor:_")
    implementation("org.koin:koin-logger-slf4j:_")

    implementation("io.rsocket.kotlin:rsocket-core:_")
    implementation("io.rsocket.kotlin:rsocket-transport-ktor:_")

//    implementation("org.xerial:sqlite-jdbc:_")
    implementation("com.squareup.sqldelight:sqlite-driver:_")

    implementation("org.postgresql:postgresql:_")

    testImplementation(kotlin("test-junit"))

    //TODO: remove

    implementation("org.jetbrains.exposed:exposed-core:_")
    implementation("org.jetbrains.exposed:exposed-dao:_")
    implementation("org.jetbrains.exposed:exposed-jdbc:_")

    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-databind:_")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:_")
}

kotlin {
    target {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

sqldelight {
    database("Database") {
        packageName = "server.db"
//        dialect = "postgres"
        verifyMigrations = true
        deriveSchemaFromMigrations = true

        schemaOutputDirectory = file("src/main/sqldelight/databases")
    }
}

//TODO: add back once working again
/*
val packageStatic = tasks.create("packageStatic") {
    group = "build"
    val frontend = project(":frontend")
    dependsOn(":frontend:processResources")
    if(properties.contains("dev")) {
        dependsOn(":frontend:browserDevelopmentWebpack")
    } else {
        dependsOn(":frontend:browserProductionWebpack")
    }

    outputs.upToDateWhen { false }
    outputs.dir(gen_resource)

    val staticFolder = gen_resource.resolve("static").apply { mkdirs() }

    doFirst {
        staticFolder.deleteRecursively()
        staticFolder.mkdirs()
        copy {
            from(frontend.tasks.get("processResources"))
            from(frontend.buildDir.resolve("distributions"))
            into(staticFolder)
        }
    }

//    from(terserTask)
//    from(bundleTask)

    doLast {
//        copy {
//            from(frontend.buildDir.resolve("distributions"))
//            into(staticFolder.resolve("js"))
//        }
    }
}
*/

application {
    mainClassName = "io.ktor.server.cio.EngineMain"
}

/*
val shadowJar = tasks.getByName<ShadowJar>("shadowJar") {
    dependsOn(packageStatic)

    from(packageStatic)
}
*/
val shadowJar by tasks.getting(ShadowJar::class)

val unzipJsJar = tasks.create<Copy>("unzipShadowJar") {
    dependsOn(shadowJar)
    group = "build"
    from(zipTree(shadowJar.archiveFile))
    into(shadowJar.destinationDirectory.file(shadowJar.archiveBaseName))
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
