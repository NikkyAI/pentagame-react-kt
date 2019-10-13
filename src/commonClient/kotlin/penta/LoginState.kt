package penta

import io.ktor.http.Url

sealed class LoginState {
    interface NotLoggedIn
    object Disconnected: LoginState(), NotLoggedIn
    class UserIDRejected(
        val userId: String,
        val reason: String
    ): LoginState(), NotLoggedIn
    class RequiresPassword(val baseUrl: Url, val userId: String): LoginState(), NotLoggedIn
    class Connected(
        val baseUrl: Url,
        val userId: String,
        var session: String
    ): LoginState() {

    }
}
