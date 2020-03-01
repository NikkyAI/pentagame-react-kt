package penta

import SessionEvent
import com.soywiz.klogger.Logger
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.http.Url
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import penta.network.GameSessionInfo
import penta.network.GameEvent
import penta.util.json

sealed class ConnectionState {
    companion object {
        private val logger = Logger(this::class.simpleName!!)
    }

    abstract val baseUrl: Url
    abstract val userId: String

    interface NotLoggedIn

    class Unreachable(
        override val baseUrl: Url = Url("https://pentagame.herokuapp.com"),
        override val userId: String = ""
    ) : ConnectionState(), NotLoggedIn

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
        override var session: String,
        internal val websocketSessionLobby: DefaultClientWebSocketSession
    ) : ConnectionState(), HasSession {
        suspend fun sendMessage() {
            TODO("copy from sendMove")
        }

        suspend fun disconnect() {
            logger.info { "leaving game" }
            logger.info { "sending close frame" }
            websocketSessionLobby.close(CloseReason(CloseReason.Codes.NORMAL, "leaving game"))
            websocketSessionLobby.terminate()
        }
    }

    data class ConnectedToGame(
        override val baseUrl: Url,
        override val userId: String,
        override var session: String,
        override val game: GameSessionInfo,
        val isPlayback: Boolean = false,
        private val websocketSessionGame: DefaultClientWebSocketSession,
        private val websocketSessionLobby: DefaultClientWebSocketSession
    ) : ConnectionState(), HasSession, HasGameSession {
        @Deprecated("send session instead")
        suspend fun sendMove(move: PentaMove) {
            websocketSessionGame.outgoing.send(
                Frame.Text(json.stringify(SessionEvent.serializer(), SessionEvent.WrappedGameEvent(move.toSerializable())))
            )
        }
        suspend fun sendEvent(event: SessionEvent) {
            websocketSessionGame.outgoing.send(
                Frame.Text(json.stringify(SessionEvent.serializer(), event))
            )
        }
        suspend fun sendSessionEvent(event: SessionEvent) {
            websocketSessionGame.outgoing.send(
                Frame.Text(json.stringify(SessionEvent.serializer(), event))
            )
        }

        suspend fun leave() {
            logger.info { "leaving game" }
//            logger.info { "sending close request" }
//            running = false
//            websocketSession.outgoing.send(Frame.Text("close"))
            logger.info { "sending close frame" }
            websocketSessionGame.close(CloseReason(CloseReason.Codes.NORMAL, "leaving game"))
            websocketSessionGame.terminate()
//            logger.info { "finished leaving game" }
        }
    }

}
