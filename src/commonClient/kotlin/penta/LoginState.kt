package penta

import io.ktor.http.Url

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
    open class Connected(
        override val baseUrl: Url,
        override val userId: String,
        open var session: String
    ): LoginState()
    class Playing(
        override val baseUrl: Url,
        override val userId: String,
        override var session: String,
        val gameId: String
    ): Connected(baseUrl, userId, session)
}
