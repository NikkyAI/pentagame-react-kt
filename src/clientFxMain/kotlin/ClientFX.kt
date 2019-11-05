import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.cookies.AcceptAllCookiesStorage
import io.ktor.client.features.cookies.HttpCookies
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.websocket.WebSockets
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import penta.app.TrayUtil
import penta.json
import java.awt.TrayIcon

actual val client: HttpClient = HttpClient(CIO).config {
    install(WebSockets)
    install(JsonFeature) {
        serializer = KotlinxSerializer(json)
    }
    install(HttpCookies) {
        // Will keep an in-memory map with all the cookies from previous requests.
        storage = AcceptAllCookiesStorage()
    }
}

actual val clientDispatcher = Dispatchers.JavaFx as CoroutineDispatcher

actual fun showNotification(title: String, body: String) {
    TrayUtil.trayIcon.displayMessage(title, body, TrayIcon.MessageType.INFO)
}