package org.gradle.kotlin.dsl

fun ktor(module: String? = null, version: String? = null): Any =
    "io.ktor:${module?.let { "ktor-$module" } ?: "ktor"}:${version ?: Ktor.version}"
