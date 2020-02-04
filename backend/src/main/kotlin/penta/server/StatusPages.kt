package penta.server

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import java.io.PrintWriter
import java.io.StringWriter

fun Application.install() {
    install(StatusPages) {
        exception<Throwable> { cause ->
            cause.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                StackTraceMessage(cause)
            )
        }
        exception<IllegalArgumentException> { cause ->
            call.respond(
                HttpStatusCode.NotAcceptable,
                StackTraceMessage(cause)
            )
        }
        exception<NumberFormatException> { cause ->
            call.respond(
                HttpStatusCode.NotAcceptable,
                StackTraceMessage(cause)
            )
        }
    }
}

val Throwable.stackTraceString: String
    get() {
        val sw = StringWriter()
        this.printStackTrace(PrintWriter(sw))
        return sw.toString()
    }

data class StackTraceMessage(private val e: Throwable) {
    val exception: String = e.javaClass.name
    val message: String = e.message ?: ""
    val stacktrace: List<String> = e.stackTraceString.split('\n')
}