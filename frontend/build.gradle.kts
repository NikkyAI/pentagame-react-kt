plugins {
    kotlin("js")
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
        useCommonJs()
        browser()
        compilations.all {
            kotlinOptions {
                sourceMap = true
                metaInfo = true
                moduleKind = "amd"
                sourceMapEmbedSources = "always"
            }
        }
    }


    sourceSets {
        val main by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))

                implementation(npm("react", "^16.9.0"))
                implementation(npm("react-dom", "^16.9.0"))
                implementation(npm("styled-components", "^4.4.1"))
                implementation(npm("inline-style-prefixer", "^5.1.0"))
                implementation(npm("core-js", "^3.4.7"))
                implementation(npm("css-in-js-utils", "^3.0.2"))

                var kotlinWrappersVersion = "pre.88-kotlin-1.3.60"
                implementation("org.jetbrains:kotlin-react:16.9.0-${kotlinWrappersVersion}")
                implementation("org.jetbrains:kotlin-react-dom:16.9.0-${kotlinWrappersVersion}")
                implementation("org.jetbrains:kotlin-css:1.0.0-${kotlinWrappersVersion}")
                implementation("org.jetbrains:kotlin-css-js:1.0.0-${kotlinWrappersVersion}")
                implementation("org.jetbrains:kotlin-styled:1.0.0-${kotlinWrappersVersion}")

//                implementation(npm("kotlinx-coroutines-core","^1.3.2"))
//                implementation(npm("kotlinx-html","0.6.12"))


                implementation(project(":shared"))

                // temp fx ?
                implementation(npm("text-encoding"))
            }
        }

        val test by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))


                // temp fx ?
//                implementation(npm("text-encoding"))
            }
        }
    }
}


