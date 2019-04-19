plugins {
    `kotlin-dsl`// version "1.3.30"
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://dl.bintray.com/kotlin/kotlinx.html/") {
        name = "kotlinx bintray"
    }
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-html-jvm:0.6.12")
}