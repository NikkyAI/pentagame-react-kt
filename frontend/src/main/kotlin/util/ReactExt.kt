package util

import react.useMemo

inline fun <reified T>memo(vararg dependencies: Any?, noinline callback: (T) -> Unit): (T)->Unit {
    return useMemo({callback}, dependencies)
}