package org.gradle.kotlin.dsl

import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import java.net.URI

fun ktor(module: String? = null, version: String? = null): Any =
    "io.ktor:${module?.let { "ktor-$module" } ?: "ktor"}:${version ?: Ktor.version}"

fun d2v(module: String, version: String? = Data2Viz.version): String =
    "${Data2Viz.group}:${module}" + (version?.let { ":$it" } ?: "")

fun Project.captureExec(vararg args: Any): String {
    return ByteArrayOutputStream().use { os ->
        val result = exec {
            commandLine(*args)
            standardOutput = os
        }
        os.toString()
    }
}

data class JDBC(
    val host: String,
    val database: String,
    val user: String?,
    val password: String?
)

fun Project.split_jdbc(connectionString: String): JDBC {
    val cleanURI = connectionString.substring(5)

    val uri = URI.create(cleanURI)
//    logger.info(uri.scheme)
//    logger.info(uri.host)
//    logger.info(uri.userInfo)
//    logger.info(uri.query)
//    logger.info("" + uri.port)
//    logger.info(uri.path)

    val query = uri.query.split('&').map {
        val (a, b) = it.split('=')
        a to b
    }.toMap()

    return JDBC(
        host = uri.host,
        database = uri.path.substringAfterLast('/'),
        user = query["user"],
        password = query["password"]
    )

}

fun Project.pg_dump(connectionString: String, target: String, extraArgs: Array<String>) {
    val cleanURI = connectionString.substring(5)

    val uri = URI.create(cleanURI)
//    logger.info(uri.scheme)
//    logger.info(uri.host)
//    logger.info(uri.userInfo)
//    logger.info(uri.query)
//    logger.info("" + uri.port)
//    logger.info(uri.path)

    val query = uri.query.split('&').map {
        val (a, b) = it.split('=')
        a to b
    }.toMap()

    pg_dump(uri.host, uri.path.substringAfterLast('/'), target, query["user"], query["password"], extraArgs)
}

fun Project.pg_dump(host: String, database: String, target: String, user: String? = null, password: String? = null, extraArgs: Array<String> = arrayOf()) {

    exec {
        password?.let {
            environment("PGPASSWORD", it)
        }
        commandLine(
            "pg_dump", *extraArgs,
            "-h", host,
            "-U", user ?: "",
            "-d", database,
            "-f", target
        )
        logger.lifecycle("executing: "+ commandLine.joinToString(" "))
    }
}