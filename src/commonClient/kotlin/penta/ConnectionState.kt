package penta

import io.ktor.http.Url

sealed class ConnectionState {
    interface NotLoggedIn
    object Disconnected: ConnectionState(), NotLoggedIn
    class RequiresPassword(val baseUrl: Url, val userId: String): ConnectionState(), NotLoggedIn
    class Connected(val baseUrl: Url, val userId: String): ConnectionState()
}