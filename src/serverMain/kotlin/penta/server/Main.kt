package penta.server

import io.ktor.server.cio.CIO
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule
import penta.SerialNotation

@KtorExperimentalAPI
fun main(args: Array<String>) {
    embeddedServer(CIO, commandLineEnvironment(args))
        .start(wait = true)
}