package penta.util

inline fun <E> MutableList<E>.replaceLast(replace: E.() -> E) {
    set(lastIndex, replace(last()))
}