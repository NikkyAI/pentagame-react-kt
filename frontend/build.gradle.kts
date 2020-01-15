plugins {
    kotlin("js")
}

kotlin {
    target {
        // new kotlin("js") stuff
        useCommonJs()
        browser {
            runTask {
                sourceMaps = true
            }
            webpackTask {
                sourceMaps = true
            }
        }
        compilations.all {
            kotlinOptions {
                sourceMap = true
//                metaInfo = true
                moduleKind = "amd"
                sourceMapEmbedSources = "always"
            }
        }
    }


    sourceSets {
        val main by getting {
//            dependencies {
//                implementation(kotlin("stdlib-js"))
//
//                implementation(project(":shared"))
//
//                api(npm("redux-logger"))
//
//                // temp fix ?
//                implementation(npm("text-encoding"))
//            }
        }

        val test by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
            }
        }
    }
}

dependencies {
    implementation(kotlin("stdlib-js"))

    implementation(project(":shared"))

    implementation(npm("redux-logger"))

// material UI components
//    implementation(npm("@material-ui/core", "^4.4.2"))
//    implementation(npm("@material-ui/icons", "^4.4.1"))
////                api(npm("core-js", "^3.1.4"))
//    implementation(npm("react-jss"))
//    implementation(npm("core-js"))
//    implementation("com.ccfraser.muirwik:muirwik-components:0.2.2")

    // temp fix ?
    implementation(npm("text-encoding"))

//    api(npm("react-bootstrap"))
//    api(npm("@types/react-bootstrap"))
}

//val JsJar = tasks.getByName<Jar>("JsJar")
//
//val unzipJsJar = tasks.create<Copy>("unzipJsJar") {
//    dependsOn(JsJar)
//    group = "build"
//    from(zipTree(JsJar.archiveFile))
//    into(JsJar.destinationDirectory.file(JsJar.archiveBaseName))
//}

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
