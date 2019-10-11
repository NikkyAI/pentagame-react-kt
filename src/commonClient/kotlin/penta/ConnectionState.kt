package penta

import io.ktor.http.Url

sealed class ConnectionState {
    object Disconnected: ConnectionState()
    class Connected(val url: Url): ConnectionState()
}