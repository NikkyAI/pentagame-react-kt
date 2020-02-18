import de.undercouch.gradle.tasks.download.org.apache.commons.codec.digest.DigestUtils
plugins {
    kotlin("js")
    id("de.fayard.dependencies")
}

kotlin {
    target {
        // new kotlin("js") stuff
        useCommonJs()
        browser {
            dceTask {
                dceOptions {
                    keep("ktor-ktor-io.\$\$importsForInline\$\$.ktor-ktor-io.io.ktor.utils.io")
                }
            }
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

tasks.getByName<ProcessResources>("processResources") {
    val downloadCss = mutableMapOf<String, String>()
    val processCss = mutableMapOf<String, String>()

    filesMatching("*.html") {
        filter {  content ->
            val regex = Regex("<link rel=\"stylesheet\" href=\"(.+)\" />")
            content.replace(regex) { matchResult ->
                val url = matchResult.groupValues[1]
                val filename = url.substringAfterLast('/')
                val hashed = "css/" + DigestUtils.md5Hex(url) + ".css"
                downloadCss += hashed to url
                "<link rel=\"stylesheet\" href=\"$hashed\" /> <!-- $filename -->"
            }
        }
    }

    doLast {
        val dir = buildDir.resolve("processedResources")
            .resolve("Js")
            .resolve("main")
        downloadCss.forEach { filename, url ->
            logger.lifecycle("downloading $url -> $filename")
            val destFile = dir.resolve(filename)
            destFile.parentFile.mkdirs()
            destFile.createNewFile()
            downloadFile(url, destFile)

            logger.lifecycle("filtering $filename")
            val ttfUrlRegex = Regex("""url\((http.*?\.ttf)\)""")
            destFile.writeText(
                destFile.readText().replace(ttfUrlRegex) { result ->
                    val ttfUrl = result.groupValues[1]
//                    val filename = ttfUrl.substringAfterLast('/')
                    val newTtfPath = "ttf/" + DigestUtils.md5Hex(ttfUrl) + ".ttf"
                    val ttfFile = dir.resolve(newTtfPath)
                    logger.lifecycle("downloading $ttfUrl -> $newTtfPath")
                    ttfFile.parentFile.mkdirs()
                    ttfFile.createNewFile()
                    downloadFile(ttfUrl, ttfFile)

                    logger.lifecycle("replacing: $ttfUrl -> $newTtfPath")
                    "url(../$newTtfPath) /* $url */ "
                }
            )

        }
    }
}

/***
//TODO: error Task with name 'browserProductionWebpack' not found in project ':frontend'.

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
**/
