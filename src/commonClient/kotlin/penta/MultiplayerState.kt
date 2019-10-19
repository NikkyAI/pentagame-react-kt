package penta

import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.http.Url
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import mu.KotlinLogging
import penta.network.GameSessionInfo

sealed class MultiplayerState {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    abstract val baseUrl: Url
    abstract val userId: String
    interface NotLoggedIn
    data class Disconnected(
        override val baseUrl: Url = Url("http://127.0.0.1:55555"),
        override val userId: String = ""
    ): MultiplayerState(), NotLoggedIn
    data class UserIDRejected(
        override val baseUrl: Url,
        override val userId: String,
        val reason: String
    ): MultiplayerState(), NotLoggedIn
    data class RequiresPassword(
        override val baseUrl: Url,
        override val userId: String
    ): MultiplayerState(), NotLoggedIn

    interface HasSession {
        var session: String
    }
    interface HasGameSession {
        val game: GameSessionInfo
    }

    data class Connected(
        override val baseUrl: Url,
        override val userId: String,
        override var session: String
    ): MultiplayerState(), HasSession

    data class Observing(
        override val baseUrl: Url,
        override val userId: String,
        override var session: String,
        override val game: GameSessionInfo,
        private val websocketSession: DefaultClientWebSocketSession
    ): MultiplayerState(), HasSession, HasGameSession {
        suspend fun sendMove(move: PentaMove) {
            websocketSession.outgoing.send(
                Frame.Text(json.stringify(SerialNotation.serializer(), move.toSerializable()))
            )
        }
        suspend fun leave() {
            logger.info { "leaving game" }
            websocketSession.close(CloseReason(CloseReason.Codes.NORMAL, "leaving game"))
            logger.info { "finished leaving game" }
        }
    }
}
