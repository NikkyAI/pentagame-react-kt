package penta.util

import mu.KLogger

suspend fun KLogger.suspendTrace(msg: suspend () -> Any?) {
    val msgValue = msg()
    trace { msgValue }
}
suspend fun KLogger.suspendDebug(msg: suspend () -> Any?) {
    val msgValue = msg()
    debug { msgValue }
}
suspend fun KLogger.suspendInfo(msg: suspend () -> Any?) {
    val msgValue = msg()
    info { msgValue }
}
suspend fun KLogger.suspendWarn(msg: suspend () -> Any?) {
    val msgValue = msg()
    warn { msgValue }
}
suspend fun KLogger.suspendError(msg: suspend () -> Any?) {
    val msgValue = msg()
    error { msgValue }
}

suspend fun KLogger.suspendTrace(e: Throwable?, msg: suspend () -> Any?) {
    val msgValue = msg()
    trace(e) { msgValue }
}
suspend fun KLogger.suspendDebug(e: Throwable?, msg: suspend () -> Any?) {
    val msgValue = msg()
    debug(e) { msgValue }
}
suspend fun KLogger.suspendInfo(e: Throwable?, msg: suspend () -> Any?) {
    val msgValue = msg()
    info(e) { msgValue }
}
suspend fun KLogger.suspendWarn(e: Throwable?, msg: suspend () -> Any?) {
    val msgValue = msg()
    warn(e) { msgValue }
}
suspend fun KLogger.suspendError(e: Throwable?, msg: suspend () -> Any?) {
    val msgValue = msg()
    error(e) { msgValue }
}