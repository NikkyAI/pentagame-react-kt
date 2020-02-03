package tests

import com.soywiz.klogger.Logger
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import penta.server.db.User
import kotlin.test.BeforeTest
import kotlin.test.Test

val logger = Logger("DBTests")

@BeforeTest
fun connect() {
    Database.connect(
        url = "jdbc:postgresql://localhost:5432/pentagame",
        user = "postgres",
        driver = "org.postgresql.Driver"
    )
}

@Test
fun `users select all`() {

    val users =  transaction {
        addLogger(Slf4jSqlDebugLogger)
        User.all()
    }

    logger.info { "users: $users" }
}