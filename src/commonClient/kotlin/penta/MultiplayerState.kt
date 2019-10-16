package penta

import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.http.Url
import io.ktor.http.cio.websocket.Frame

sealed class MultiplayerState {
    abstract val baseUrl: Url
    abstract val userId: String
    interface NotLoggedIn
    class  Disconnected(
        override val baseUrl: Url = Url("http://127.0.0.1:55555"),
        override val userId: String = ""
    ): MultiplayerState(), NotLoggedIn
    class UserIDRejected(
        override val baseUrl: Url,
        override val userId: String,
        val reason: String
    ): MultiplayerState(), NotLoggedIn
    class RequiresPassword(
        override val baseUrl: Url,
        override val userId: String
    ): MultiplayerState(), NotLoggedIn

    interface HasSession

    open class Connected(
        override val baseUrl: Url,
        override val userId: String
    ): MultiplayerState(), HasSession

    class Playing(
        override val baseUrl: Url,
        override val userId: String,
        val gameId: String,
        private val websocketSession: DefaultClientWebSocketSession
    ): MultiplayerState(), HasSession {
        suspend fun sendMove(move: PentaMove) {
            websocketSession.outgoing.send(
                Frame.Text(json.stringify(SerialNotation.serializer(), move.toSerializable()))
            )
        }
    }
}
