import  de.fayard.versions.setupVersionPlaceholdersResolving

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
    dependencies.classpath("de.fayard.refreshVersions:de.fayard.refreshVersions.gradle.plugin:0.8.6")
}

plugins {
  id("com.gradle.enterprise").version("3.1.1")
}

settings.setupVersionPlaceholdersResolving()

enableFeaturePreview("GRADLE_METADATA")

includeBuild("ksvg")

include("backend")
include("frontend")
include("shared")
include(":muirwik")

project(":muirwik").projectDir = rootDir.resolve("muirwik/muirwik-components")


gradleEnterprise {
    buildScan {
        termsOfServiceAgree = "yes"
        publishAlwaysIf(true)
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
    }
}