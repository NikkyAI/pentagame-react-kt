package penta

import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.http.Url
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import mu.KotlinLogging
import penta.network.GameEvent
import penta.network.GameSessionInfo
import penta.util.json

sealed class ConnectionState {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    // tmp fix
    val value: ConnectionState get() = this

    abstract val baseUrl: Url
    abstract val userId: String

    interface NotLoggedIn
    data class Disconnected(
        override val baseUrl: Url = Url("https://pentagame.herokuapp.com"),
        override val userId: String = ""
    ) : ConnectionState(), NotLoggedIn

    data class UserIDRejected(
        override val baseUrl: Url,
        override val userId: String,
        val reason: String
    ) : ConnectionState(), NotLoggedIn

    data class RequiresPassword(
        override val baseUrl: Url,
        override val userId: String
    ) : ConnectionState(), NotLoggedIn

    interface HasSession {
        var session: String
    }

    interface HasGameSession {
        val game: GameSessionInfo
    }

    data class Authenticated(
        override val baseUrl: Url,
        override val userId: String,
        override var session: String
    ) : ConnectionState(), HasSession

    data class Lobby(
        override val baseUrl: Url,
        override val userId: String,
        override var session: String
    ) : ConnectionState(), HasSession

    data class Observing(
        override val baseUrl: Url,
        override val userId: String,
        override var session: String,
        override val game: GameSessionInfo,
        private val websocketSession: DefaultClientWebSocketSession,
        var running: Boolean
    ) : ConnectionState(), HasSession, HasGameSession {
        suspend fun sendMove(move: PentaMove) {
            websocketSession.outgoing.send(
                Frame.Text(json.stringify(GameEvent.serializer(), move.toSerializable()))
            )
        }

        suspend fun leave() {
            logger.info { "leaving game" }
//            logger.info { "sending close request" }
//            running = false
//            websocketSession.outgoing.send(Frame.Text("close"))
            logger.info { "sending close frame" }
            websocketSession.close(CloseReason(CloseReason.Codes.NORMAL, "leaving game"))
            websocketSession.terminate()
//            logger.info { "finished leaving game" }
        }
    }
}
