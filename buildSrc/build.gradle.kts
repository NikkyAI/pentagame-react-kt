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
    api(group = "com.marvinformatics.apgdiff", name = "apgdiff", version = "2.5.0.20160618")
}
