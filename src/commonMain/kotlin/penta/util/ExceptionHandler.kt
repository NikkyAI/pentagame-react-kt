package penta.util

import kotlinx.coroutines.CoroutineExceptionHandler

val handler = CoroutineExceptionHandler { _, exception ->
    println("Caught $exception")
}
