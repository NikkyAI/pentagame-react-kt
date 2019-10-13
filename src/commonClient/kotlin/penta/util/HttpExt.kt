package penta.util

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import penta.LoginState

suspend fun <T> HttpResponse.parse(serializer: KSerializer<T>, json: Json = penta.json): T = json.parse(
    serializer,
    readText()
)
fun HttpRequestBuilder.authenticateWith(state: LoginState.Connected) {
    header("SESSION", state.session)
}
