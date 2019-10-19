import com.lightningkite.koolui.async.UI
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

expect val client: HttpClient

expect val clientDispatcher: CoroutineDispatcher