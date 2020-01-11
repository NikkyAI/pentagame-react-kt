plugins {
    kotlin("js")
//    id("kotlin2js")
//    id("org.jetbrains.kotlin.frontend")
//    id("kotlin-dce-js")
}

repositories {
    mavenLocal()
    jcenter()
    maven("https://dl.bintray.com/kotlin/kotlin-js-wrappers")
    maven("https://dl.bintray.com/kotlin/kotlinx")
    mavenCentral()
}

repositories {
    mavenCentral()
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
            dependencies {
                implementation(kotlin("stdlib-js"))

                implementation(project(":shared"))

                api(npm("redux-logger"))

                // temp fix ?
                implementation(npm("text-encoding"))
            }
        }

        val test by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
            }
        }
    }
}

val bundleDir = buildDir.resolve("full_bundle")//.apply { mkdirs() }
val browserWebpack = tasks.getByName<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>("browserWebpack") {

}

val processResources = tasks.getByName("processResources")

val bundle = tasks.create("bundle") {
    group = "build"
    dependsOn(processResources)
    dependsOn(browserWebpack)

    outputs.upToDateWhen { false }
    outputs.dir(bundleDir)

//    val staticFolder = gen_resource.resolve("static").apply { mkdirs() }
//
    doFirst {
        bundleDir.deleteRecursively()
        bundleDir.mkdirs()
    }



    // TODO: readd terser and require.js for dev
//    from(terserTask)
//    from(bundleTask)

    doLast {
        copy {
            from(processResources)
//            from(browserWebpack)
//            from(buildDir.resolve("processedResources/js/main"))
            from(buildDir.resolve("bundle"))
            from(buildDir.resolve("distributions"))
            into(bundleDir)
        }
    }
}

val JsJar = tasks.getByName<Jar>("JsJar")

val unzipJsJar = tasks.create<Copy>("unzipJsJar") {
    dependsOn(JsJar)
    group = "build"
    from(zipTree(JsJar.archiveFile))
    into(JsJar.destinationDirectory.file(JsJar.archiveBaseName))
}

//kotlin {
//    target {
//        compilations.all {
//            kotlinOptions {
//                sourceMap = true
////                metaInfo = true
//                moduleKind = "amd"
//                sourceMapEmbedSources = "always"
//            }
//        }
//    }
//
//    sourceSets {
//        val main by getting {
//            dependencies {
//                implementation(kotlin("stdlib-js"))
//                implementation(project(":shared"))
//
//                // temp fix ?
////                implementation(npm("text-encoding"))
//            }
//        }
//
//        val test by getting {
//            dependencies {
//                implementation(kotlin("stdlib-js"))
//            }
//        }
//    }
//}

//kotlinFrontend {
//    sourceMaps = true
//    npm {
//        devDependency("terser")
//    }
//    bundle("webpack", delegateClosureOf<org.jetbrains.kotlin.gradle.frontend.webpack.WebPackExtension> {
////        bundleName = "this-will-be-overwritten" // NOTE: for example purposes this is overwritten in `webpack.config.d/filename.js`.
//        contentPath = file("src/main/resources/web")
//        if (project.hasProperty("prod")) {
//            mode = "production"
//        }
//    })
//}

/*
clean.doFirst() {
    delete("${web_dir}")
}


bundle.doLast() {
    copy {
        from "${buildDir}/resources/main/web"
        from "${buildDir}/bundle"
        into "${web_dir}"
    }
}
 */

