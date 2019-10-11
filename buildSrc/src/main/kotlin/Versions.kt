object Jetbrains {
    object Kotlin {
        val version = "1.3.50"
    }
}

object Data2Viz {
    private const val version = "0.8.0-RC1"
//    private const val version = "master-SNAPSHOT"
    const val group = "io.data2viz"

//    open class D2VDep(val component: String) {
//        val common = "io.data2viz:d2v-$component-common:$version"
//        val jfx = "io.data2viz:d2v-$component-jfx:$version"
//        val js = "io.data2viz:d2v-$component-js:$version"
//    }
//
//    val data2viz = D2VDep("data2viz")
//    val core = D2VDep("core")
//    val axis = D2VDep("axis")
//    val color = D2VDep("color")
    const val common_dep = "$group:d2v-data2viz-common:$version"
    const val jfx_dep = "$group:d2v-data2viz-jfx:$version"
    const val js_dep = "$group:d2v-data2viz-js:$version"
}

object TornadoFX {
    const val version = "1.7.19"
    val dep = "no.tornado:tornadofx:$version"
}

object Ktor {
    const val version = "1.2.5"
}

object Serialization {
    const val version = "0.13.0"
}

object Coroutines {
    const val version = "1.3.2"
}

object Kommon {
    const val version = "0.1.8"
}

object KoolUI {
    const val version = "0.1.6"
}

object Reacktive {
    const val version = "0.1.6"
}

object Recktangle {
    const val version = "0.1.6"
}

object Lokalize {
    const val version = "0.1.6"
}