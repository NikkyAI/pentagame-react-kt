package penta.server

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.soywiz.klogger.Logger
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.XForwardedHeaderSupport
import io.ktor.http.HttpMethod
import io.ktor.jackson.jackson
import io.ktor.sessions.SessionStorageMemory
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.websocket.WebSockets
import org.jetbrains.exposed.sql.Database
import org.slf4j.event.Level
import java.time.Duration

private val logger = Logger("PentaApp")
fun Application.main() {
    Logger.defaultLevel = Logger.Level.INFO
    install(DefaultHeaders)
    // TODO: fix call logging
    install(CallLogging) {
//        logger = penta.server.logger
        level = Level.INFO
    }

    val db = Database.connect(
        url = System.getenv("JDBC_DATABASE_URL"),
        driver = "org.postgresql.Driver"
    )

    install(WebSockets) {
//        pingPeriod = Duration.ofMillis(1000)
//        timeout = Duration.ofMillis(2000)
        pingPeriodMillis = 1000
        timeoutMillis = 2000
    }

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
//        maxAge = Duration.ofMinutes(20)
        maxAgeInSeconds = 20 * 60
    }
    install(XForwardedHeaderSupport)
//    install(EncryptionEnforcementFeature)

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
