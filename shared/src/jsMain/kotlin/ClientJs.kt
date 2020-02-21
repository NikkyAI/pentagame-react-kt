import com.soywiz.klogger.Logger
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.features.cookies.AcceptAllCookiesStorage
import io.ktor.client.features.cookies.HttpCookies
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.websocket.WebSockets
import kotlinx.coroutines.Dispatchers
import org.w3c.notifications.DENIED
import org.w3c.notifications.GRANTED
import org.w3c.notifications.Notification
import org.w3c.notifications.NotificationOptions
import org.w3c.notifications.NotificationPermission
import penta.util.json

private val logger = Logger("ClientKt")
//@showcase
actual val client: HttpClient = HttpClient(Js).config {
    install(WebSockets) {

    }
    install(JsonFeature) {
        serializer = KotlinxSerializer(json)
    }
    install(HttpCookies) {
        // Will keep an in-memory map with all the cookies from previous requests.
        storage = AcceptAllCookiesStorage()
    }
}

actual val clientDispatcher = Dispatchers.Default
actual fun showNotification(title: String, body: String) {
    Notification.requestPermission().then {
        when (it) {
            NotificationPermission.GRANTED -> {
                Notification(
                    title,
                    NotificationOptions(
    //                    badge = "badge",
                        body = body
                    )
                )
            }
            NotificationPermission.DENIED -> {
                logger.error { "notification denied" }
            }
            else -> {
                logger.error { "notification denied/else" }
            }
        }
    }
}