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
                sourceMapPrefix = ""
                metaInfo = true
//                moduleKind = "amd"
//                sourceMapEmbedSources = "always"
            }
        }
    }


    sourceSets {
        val main by getting {
            dependencies {

            }
        }

        val test by getting {
            dependencies {
                implementation(kotlin("stdlib"))
            }
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation(project(":shared"))

    // temp fix ?
//    implementation(npm("text-encoding"))
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

tasks.getByName("browserProductionWebpack").apply {
    doLast {
        val rootDirPath = rootDir.absolutePath.replace('\\', '/')
        val mapFile = project.buildDir.resolve("distributions/${project.name}.js.map")
        mapFile.writeText(
            mapFile.readText()
                .replace("$rootDirPath/build/js/src/main/", "")
                .replace("$rootDirPath/build/src/main/", "")
                .replace("$rootDirPath/build/js/node_modules", "node_modules")
                .replace(rootDirPath, rootDir.name)
        )
    }
}
