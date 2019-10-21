package penta.server

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.sessions.SessionStorageMemory
import io.ktor.http.HttpMethod
import io.ktor.jackson.jackson
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.sessions.header
import io.ktor.websocket.WebSockets
import mu.KotlinLogging
import org.slf4j.event.Level
import java.time.Duration

private val logger = KotlinLogging.logger {}
fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging) {
        logger = penta.server.logger
        level = Level.INFO
    }

    install(WebSockets)

//    install(HttpsRedirect)
//    install(HSTS)
    install(CORS) {
        anyHost()
        method(HttpMethod.Get)
        allowNonSimpleContentTypes = true
        allowSameOrigin = true
        allowCredentials = true
        header("SESSION")
//        header("Set-Cookie")
        exposeHeader("SESSION")
//        exposeHeader("Set-Cookie")
        maxAge = Duration.ofMinutes(20)
    }
    install(XForwardedHeaderSupport)
    install(EncryptionEnforcementFeature)
//    install(Metrics) {
//        val reporter = Slf4jReporter.forRegistry(registry)
//                .outputTo(log)
//                .convertRatesTo(TimeUnit.SECONDS)
//                .convertDurationsTo(TimeUnit.MILLISECONDS)
//                .build()
//        reporter.start(10, TimeUnit.SECONDS)
//    }
    install(ContentNegotiation) {
        jackson {
            registerModule(KotlinModule()) // Enable Kotlin support
            enable(SerializationFeature.INDENT_OUTPUT)
//            enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
        }
    }
    install(Sessions) {
        cookie<UserSession>("SESSION", storage = SessionStorageMemory()) {
            cookie.path = "/" // Specify cookie's path '/' so it can be used in the whole site
        }
    }

}