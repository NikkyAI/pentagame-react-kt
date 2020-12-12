import de.fayard.refreshVersions.bootstrapRefreshVersions

buildscript {
    repositories { gradlePluginPortal() }
    dependencies.classpath("de.fayard.refreshVersions:refreshVersions:0.9.7")
}

plugins {
  id("com.gradle.enterprise").version("3.5")
}

bootstrapRefreshVersions(
    listOf(rootDir.resolve("buildSrc/dependencies-rules.txt").readText())
)

//includeBuild("ksvg")

include("backend")
//include("frontend")
include("shared")
//include(":muirwik")

//project(":ksvg").projectDir = rootDir.resolve("ksvg")
//project(":muirwik").projectDir = rootDir.resolve("muirwik/muirwik-components")


gradleEnterprise {
    buildScan {
        termsOfServiceAgree = "yes"
//        publishAlwaysIf(true)
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
    }
}