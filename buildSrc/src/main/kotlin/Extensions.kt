package org.gradle.kotlin.dsl

import org.gradle.api.Project
import java.io.ByteArrayOutputStream

fun ktor(module: String? = null, version: String? = null): Any =
    "io.ktor:${module?.let { "ktor-$module" } ?: "ktor"}:${version ?: Ktor.version}"

fun Project.captureExec(vararg args: Any): String {
    return ByteArrayOutputStream().use { os ->
        val result = exec {
            commandLine(*args)
            standardOutput = os
        }
        os.toString()
    }
}
