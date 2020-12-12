package tests

import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import old.server.db.User
import kotlin.test.BeforeTest
import kotlin.test.Test

val logger = KotlinLogging.logger("DBTests")

@BeforeTest
fun connect() {
    Database.connect(
        url = System.getenv("DEV_DATABASE_URL"),
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