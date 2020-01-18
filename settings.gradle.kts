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

enableFeaturePreview("GRADLE_METADATA")

rootProject.name = "penta"
include("backend")
include("frontend")
include("shared")
include(":muirwik")

project(":muirwik").projectDir = rootDir.resolve("muirwik/muirwik-components")
