package penta.server

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.websocket.webSocket
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.serialization.list
import kotlinx.serialization.serializer
import penta.SerialNotation
import penta.json
import penta.network.ServerStatus

fun Application.routes() = routing {
    val received = mutableListOf<String>()
    webSocket("/") {
        // websocketSession
        while (true) {
            val frame = incoming.receive()
            when (frame) {
                is Frame.Text -> {
                    val text = frame.readText()
                    outgoing.send(Frame.Text("YOU SAID: $text"))
                    if (text.equals("bye", ignoreCase = true)) {
                        close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                    }
                }
            }
        }
    }
    webSocket("/echo") {
        println("onConnect")
        try {
            while (true) {
                val text = (incoming.receive() as Frame.Text).readText()
                println("onMessage")
                received += text
                outgoing.send(Frame.Text(text))
            }
        } catch (e: ClosedReceiveChannelException) {
            println("onClose ${closeReason.await()}")
        } catch (e: Throwable) {
            println("onError ${closeReason.await()}")
            e.printStackTrace()
        }
    }
    webSocket("/replay") {
        println("onConnect")

        val gameJson = (incoming.receive() as Frame.Text).readText()

        val notationList = json.parse(SerialNotation.serializer().list, gameJson)


        notationList.forEach {
            delay(500)
            val notationJson = json.stringify(SerialNotation.serializer(), it)
            println("sending: $notationJson")
            outgoing.send(Frame.Text(notationJson))
        }
        println("done")

        close(CloseReason(CloseReason.Codes.NORMAL, "Replay done"))
//        try {
//            while (true) {
//                val text = (incoming.receive() as Frame.Text).readText()
//                println("onMessage")
//                received += text
//                outgoing.send(Frame.Text(text))
//            }
//        } catch (e: ClosedReceiveChannelException) {
//            println("onClose ${closeReason.await()}")
//        } catch (e: Throwable) {
//            println("onError ${closeReason.await()}")
//            e.printStackTrace()
//        }
    }
    get("/api/status") {
        println("received status request")
        call.respondText(
            text = json.stringify(
                ServerStatus.serializer(),
                ServerStatus(
                    totalPlayers = 0
                )
            ),
            contentType = ContentType.Application.Json
        )
    }
    get("/api/user/{userid}") {
        val userid = call.parameters["userid"]
        call.respondText(
            contentType = ContentType.Application.Json,
            status = HttpStatusCode.NotFound,
            text = "No such user by id '$userid'"
        )
    }
    get("/api/games/") {
        call.respondText(
            contentType = ContentType.Application.Json,
            text = json.stringify(
                String.serializer().list,
                listOf()
            )
        )
    }
    post("/login") {

    }
    webSocket("/api/ws") {
        val gameId = call.parameters["gameId"] ?: throw IllegalArgumentException("missing parameter gameId")

    }
}
