import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.websocket.WebSockets

actual val client: HttpClient = HttpClient(CIO).config {
    install(WebSockets)
    install(JsonFeature) {
        serializer = KotlinxSerializer()
    }
}

//fun main() = runBlocking {
//    launch {
//        client.ws(method = HttpMethod.Get, host = "127.0.0.1", port = 55555, path = "/echo") { // this: DefaultClientWebSocketSession
//            send(Frame.Text("Hello World"))
//
//            for (message in incoming.map { it as? Frame.Text }.filterNotNull()) {
//                println(message.readText())
//            }
//        }
//    }
//}