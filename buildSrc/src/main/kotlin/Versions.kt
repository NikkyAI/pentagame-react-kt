object Jetbrains {
    object Kotlin {
        val version = "1.3.30"
    }
}

object Data2Viz {
    private const val version = "0.7.2-RC1"

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
    const val common_dep = "io.data2viz:d2v-data2viz-common:$version"
    const val jfx_dep = "io.data2viz:d2v-data2viz-jfx:$version"
    const val js_dep = "io.data2viz:d2v-data2viz-js:$version"
}

object TornadoFX {
    const val version = "1.7.18"
    val dep = "no.tornado:tornadofx:$version"
}