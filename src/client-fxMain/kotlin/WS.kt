import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.channels.filterNotNull
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.runBlocking

actual val client: HttpClient = HttpClient(CIO).config {
            install(WebSockets)
}

fun main() = runBlocking {
    client.ws(method = HttpMethod.Get, host = "127.0.0.1", port = 55555, path = "/echo") { // this: DefaultClientWebSocketSession
        send(Frame.Text("Hello World"))

        for (message in incoming.map { it as? Frame.Text }.filterNotNull()) {
            println(message.readText())
        }
    }
}