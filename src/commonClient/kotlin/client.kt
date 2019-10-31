import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher

expect val client: HttpClient

expect val clientDispatcher: CoroutineDispatcher

expect fun showNotification(title: String, body: String)