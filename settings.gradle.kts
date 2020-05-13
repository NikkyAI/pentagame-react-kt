import de.fayard.dependencies.bootstrapRefreshVersionsAndDependencies

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven(url = "https://jcenter.bintray.com/")
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
    }

    resolutionStrategy {
        eachPlugin {
            val module = when(requested.id.id) {
                "kotlinx-serialization" -> "org.jetbrains.kotlin:kotlin-serialization:${requested.version}"
                "proguard" -> "net.sf.proguard:proguard-gradle:${requested.version}"
                else -> null
            }
            if(module != null) {
                useModule(module)
            }
        }
    }
}

buildscript {
    repositories { gradlePluginPortal() }
    dependencies.classpath("de.fayard:dependencies:0.5.8")
}

plugins {
  id("com.gradle.enterprise").version("3.1.1")
}

bootstrapRefreshVersionsAndDependencies(
    listOf(rootDir.resolve("dependencies-rules.txt").readText())
)

enableFeaturePreview("GRADLE_METADATA")

//includeBuild("ksvg")

include("backend")
include("frontend")
include("shared")
include(":muirwik")
//include(":ksvg")

//project(":ksvg").projectDir = rootDir.resolve("ksvg")
project(":muirwik").projectDir = rootDir.resolve("muirwik/muirwik-components")


gradleEnterprise {
    buildScan {
        termsOfServiceAgree = "yes"
//        publishAlwaysIf(true)
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
    }
}