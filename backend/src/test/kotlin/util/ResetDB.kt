package util

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import penta.server.db.Games
import penta.server.db.PlayingUsers
import penta.server.db.UserInGames
import penta.server.db.Users

object ResetDB {
    @JvmStatic
    fun main(args: Array<String>) {
        val db = Database.connect(
            url = System.getenv("JDBC_DATABASE_URL"),
            driver = "org.postgresql.Driver"
        )

        transaction {
            addLogger(Slf4jSqlDebugLogger)

            SchemaUtils.drop(
                Users,
                PlayingUsers,
                Games,
                UserInGames,
                inBatch = false
            )
        }

        transaction {
            addLogger(Slf4jSqlDebugLogger)

            SchemaUtils.createMissingTablesAndColumns(
                Users,
                PlayingUsers,
                Games,
                UserInGames,
                inBatch = false
            )
        }
    }
}