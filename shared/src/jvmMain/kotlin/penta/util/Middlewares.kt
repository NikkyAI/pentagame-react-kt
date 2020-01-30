package penta.util

import com.soywiz.klogger.Logger
import org.reduxkotlin.middleware

fun <T> loggingMiddleware(logger: Logger) = middleware<T> { store, next, action ->
    logger.info {
        "reduce action: $action"
    }
    //log here
    next(action)
}

fun test(c: Char) {
    c.isUpperCase()
}