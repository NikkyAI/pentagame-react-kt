package server

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.rsocket.kotlin.RSocketRequestHandler
import io.rsocket.kotlin.payload.*
import io.rsocket.kotlin.transport.ktor.server.rSocket
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

fun Application.routes() = routing {
    get("health") {
        call.respond("OK")
    }
    route("path") {

    }
}