package penta.server

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.sessions.SessionStorageMemory
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.sessions.header
import io.ktor.websocket.WebSockets
import java.time.Duration

fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)

    install(WebSockets)

//    install(HttpsRedirect)
//    install(HSTS)
    install(CORS) {
        anyHost()
        maxAge = Duration.ofMinutes(20)
    }
//    install(Metrics) {
//        val reporter = Slf4jReporter.forRegistry(registry)
//                .outputTo(log)
//                .convertRatesTo(TimeUnit.SECONDS)
//                .convertDurationsTo(TimeUnit.MILLISECONDS)
//                .build()
//        reporter.start(10, TimeUnit.SECONDS)
//    }
    install(Sessions) {
        header<MySession>("SESSION", storage = SessionStorageMemory())
    }

}