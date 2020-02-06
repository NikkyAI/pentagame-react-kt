package penta.util

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import penta.ConnectionState

suspend fun <T> HttpResponse.parse(serializer: KSerializer<T>, json: Json = penta.util.json): T = json.parse(
    serializer,
    readText()
)

fun HttpRequestBuilder.authenticateWith(state: ConnectionState.HasSession) {
//    attributes.put(AttributeKey("credentials"), "include")
    header("SESSION", state.session)
}

suspend fun HttpClient.authenticatedRequest(
    url: Url,
    state: ConnectionState.HasSession,
    method: HttpMethod,
    builder: HttpRequestBuilder.() -> Unit = {}
): HttpResponse {
    return request<HttpResponse>(url) {
        this.method = method
        authenticateWith(state)
        builder()
    }.apply {
        headers["SESSION"]?.let {
            state.session = it
        }
        check(status == HttpStatusCode.OK) { "response was $status" }
        // TODO: handle 403
    }
}

suspend fun <T : Any> HttpClient.authenticatedRequest(
    url: Url,
    state: ConnectionState.HasSession,
    method: HttpMethod,
    serializer: KSerializer<T>,
    json: Json = penta.util.json,
    builder: HttpRequestBuilder.() -> Unit = {}
): T {
    return authenticatedRequest(url, state, method, builder).run {
        json.parse(
            serializer,
            readText()
        )
    }
}
