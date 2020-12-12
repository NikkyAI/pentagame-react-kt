package old.server

import io.ktor.server.cio.CIO
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
fun main(args: Array<String>) {
    embeddedServer(CIO, commandLineEnvironment(args))
        .start(wait = true)
}