object Jetbrains {
    object Kotlin {
        val version = "1.3.61"
    }
}

object Data2Viz {
    private const val version = "0.8.0-RC1"
    const val group = "io.data2viz"

    const val common_dep = "$group:d2v-data2viz-common:$version"
    const val jfx_dep = "$group:d2v-data2viz-jfx:$version"
    const val js_dep = "$group:d2v-data2viz-js:$version"
}

object TornadoFX {
    const val version = "1.7.19"
    val dep = "no.tornado:tornadofx:$version"
}

object Ktor {
    const val version = "1.2.6"
}

object Logback {
    const val version = "1.2.3"
}

object Serialization {
    const val version = "0.14.0"
}

object Coroutines {
    const val version = "1.3.3"
}

object KotlinLogging {
    const val version = "1.7.8"
}
