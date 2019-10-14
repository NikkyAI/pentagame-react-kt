import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.websocket.WebSockets
import kotlinx.coroutines.Dispatchers
import penta.json

actual val client: HttpClient = HttpClient(Js).config {
    install(WebSockets)
    install(JsonFeature) {
        serializer = KotlinxSerializer(json)
    }
}

actual val clientDispatcher = Dispatchers.Default
