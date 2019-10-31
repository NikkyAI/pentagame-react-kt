import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.features.cookies.AcceptAllCookiesStorage
import io.ktor.client.features.cookies.HttpCookies
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.websocket.WebSockets
import kotlinx.coroutines.Dispatchers
import mu.KotlinLogging
import org.w3c.notifications.DENIED
import org.w3c.notifications.GRANTED
import org.w3c.notifications.Notification
import org.w3c.notifications.NotificationOptions
import org.w3c.notifications.NotificationPermission
import penta.json

private val logger = KotlinLogging.logger {}
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
    when (Notification.permission) {
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
            Notification.requestPermission().then {
                if (it == NotificationPermission.GRANTED) {
                    Notification(
                        "title",
                        NotificationOptions(
                            badge = "badge",
                            body = "body"
                        )
                    )
                }
            }
            logger.error { "notification denied" }
        }
    }
}