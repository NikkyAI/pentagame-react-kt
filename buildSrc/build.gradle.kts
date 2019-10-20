plugins {
    `kotlin-dsl`
    idea
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://dl.bintray.com/kotlin/kotlinx.html/") {
        name = "kotlinx bintray"
    }
}

dependencies {
    api(group = "com.squareup", name = "kotlinpoet", version = "1.4.1")
}
