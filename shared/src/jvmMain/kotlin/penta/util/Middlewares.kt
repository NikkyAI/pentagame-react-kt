package penta.util

import mu.KLogger
import org.reduxkotlin.middleware

fun <T> loggingMiddleware(logger: KLogger) = middleware<T> { store, next, action ->
    logger.info {
        "reduce action: $action"
    }
    //log here
    next(action)
}

fun test(c: Char) {
    c.isUpperCase()
}