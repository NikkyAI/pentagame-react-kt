pluginManagement {
    repositories {
        gradlePluginPortal()
        maven(url = "https://jcenter.bintray.com/")
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
    }

    resolutionStrategy {
        eachPlugin {
            val module = when(requested.id.id) {
                "kotlin2js" -> "org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}"
                "kotlin-dce-js" ->"org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}"
                "kotlinx-serialization" -> "org.jetbrains.kotlin:kotlin-serialization:${requested.version}"
                "proguard" -> "net.sf.proguard:proguard-gradle:${requested.version}"
                "org.jetbrains.kotlin.frontend" -> "org.jetbrains.kotlin:kotlin-frontend-plugin:${requested.version}"
                else -> null
            }
            if(module != null) {
                useModule(module)
            }
            //            when(requested.id.id) { }
//                "kotlin2js" -> useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
//                "kotlin-dce-js" -> useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
//                "org.jetbrains.kotlin.frontend" -> useModule("org.jetbrains.kotlin:kotlin-frontend-plugin:${requested.version}")
//            }
//            when(requested.id.id) { }
//                "kotlin2js" -> useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
//                "kotlin-dce-js" -> useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
//                "org.jetbrains.kotlin.frontend" -> useModule("org.jetbrains.kotlin:kotlin-frontend-plugin:${requested.version}")
//            }
        }
    }
}

//enableFeaturePreview("GRADLE_METADATA")

rootProject.name = "penta"
