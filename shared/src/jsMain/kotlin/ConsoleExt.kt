import kotlin.js.Console

fun Console.debug(vararg o: Any?) {
    asDynamic().debug(o)
}