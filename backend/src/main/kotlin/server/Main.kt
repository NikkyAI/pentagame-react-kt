package server

import io.ktor.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    runKtor()
}

/**
 * start embedded server and close gracefully on Ctrl-C
 */
fun runKtor() {
    val server = embeddedServer(
        factory = Netty,
        port = System.getenv("PORT")?.toIntOrNull() ?: 8080,
        host = System.getenv("HOST") ?: "127.0.0.1",
        watchPaths = listOf("jvm/main"),
        module = Application::application,
        configure = {
            // TODO: set call limits or stuff here ?
        }
    ).start(false)

    Runtime.getRuntime().addShutdownHook(Thread {
        server.stop(1, 5, TimeUnit.SECONDS)
    })
    Thread.currentThread().join()
}