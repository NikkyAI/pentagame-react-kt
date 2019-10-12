import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.websocket.WebSockets

actual val client: HttpClient = HttpClient(Js).config {
    install(WebSockets)
    install(JsonFeature) {
        serializer = KotlinxSerializer()
    }
}
