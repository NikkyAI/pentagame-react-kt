package penta

import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.http.Url
import io.ktor.http.cio.websocket.Frame

sealed class LoginState {
    abstract val baseUrl: Url
    abstract val userId: String
    interface NotLoggedIn
    class  Disconnected(
        override val baseUrl: Url = Url("http://127.0.0.1:55555"),
        override val userId: String = ""
    ): LoginState(), NotLoggedIn
    class UserIDRejected(
        override val baseUrl: Url,
        override val userId: String,
        val reason: String
    ): LoginState(), NotLoggedIn
    class RequiresPassword(
        override val baseUrl: Url,
        override val userId: String
    ): LoginState(), NotLoggedIn

    interface HasSession {
        abstract var session: String
    }
    open class Connected(
        override val baseUrl: Url,
        override val userId: String,
        override var session: String
    ): LoginState(), HasSession
    class Playing(
        override val baseUrl: Url,
        override val userId: String,
        override var session: String,
        val gameId: String,
        private val websocketSession: DefaultClientWebSocketSession
    ): LoginState(), HasSession {
        suspend fun sendMove(move: PentaMove) {
            move.toSerializableList().forEach {
                websocketSession.outgoing.send(
                    Frame.Text(json.stringify(SerialNotation.serializer(), it))
                )
            }
        }
    }
}
