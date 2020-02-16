import io.ktor.client.engine.cio.CIO
import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException
import java.net.URL

val client = HttpClient(CIO) {

}
data class HttpClientException(val response: HttpResponse) : IOException("HTTP Error ${response.status}")

fun downloadFile(url: String, file: File) {
    return runBlocking {
        val response = client.request<HttpResponse> {
            url(URL(url))
            method = HttpMethod.Get
        }
        if (!response.status.isSuccess()) {
            throw HttpClientException(response)
        }
        response.content.copyAndClose(file.writeChannel())
        file
    }
}