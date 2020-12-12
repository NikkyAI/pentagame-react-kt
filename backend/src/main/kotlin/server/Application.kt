package server

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.websocket.*
import org.koin.core.context.GlobalContext
import org.koin.ktor.ext.Koin
import org.koin.logger.SLF4JLogger
import org.slf4j.event.Level
import server.db.Database

fun Application.application() {
    install(DefaultHeaders)

    // TODO: fix call logging
    install(CallLogging) {
        //        logger = old.server.logger
        level = Level.INFO
    }

    install(Koin) {
        SLF4JLogger()
        modules(databaseModule())
    }

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

    val db = GlobalContext.get().get<Database>()
    environment.monitor.subscribe(ApplicationStopped) {

    }

    rsocket()
    routes()
}