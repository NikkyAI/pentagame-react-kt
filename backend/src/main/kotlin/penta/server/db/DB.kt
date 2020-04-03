package penta.server.db

import kotlinx.serialization.builtins.list
import kotlinx.serialization.list
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import penta.logic.GameType
import penta.network.GameEvent
import penta.util.json
import java.util.UUID

fun connect() =  Database.connect(
    url = System.getenv("JDBC_DATABASE_URL"),
    driver = "org.postgresql.Driver"
)

fun main() {
    val db = Database.connect(
        url = System.getenv("JDBC_DATABASE_URL"),
        driver = "org.postgresql.Driver"
    )

    transaction {
        addLogger(StdOutSqlLogger)

        SchemaUtils.drop(
            Users, PlayingUsers, Games, UserInGames,
            inBatch = true
        )
    }
    transaction {
        addLogger(StdOutSqlLogger)

        SchemaUtils.createMissingTablesAndColumns(
            Users, PlayingUsers, Games, UserInGames,
            inBatch = false
        )
    }

    val testUser = transaction {
        findOrCreate("TesUser") {
            userId = "TestUser"
            displayName = "Test User"
            passwordHash = "abcdefg"
            temporaryUser = false
        }
    }
    println("testUser: $testUser")

    transaction {
        addLogger(StdOutSqlLogger)

        val someuser = findOrCreate("someuser") {
            passwordHash = "abcdefgh"
            temporaryUser = true
        }

        println("someuser: $someuser")

        val newGame = Game.new(UUID.randomUUID()) {
            gameId = "game_0"
            history = json.stringify(
                GameEvent.serializer().list, listOf<GameEvent>(
//                    GameEvent.PlayerJoin(PlayerState("someuser", "tiangle")),
                    GameEvent.SetGameType(GameType.TWO),
                    GameEvent.InitGame
                )
            )
            owner = someuser
//            players = SizedCollection(
//                listOf(
//                    someuser
//                )
//            )
        }
    }

    // TODO: figure out how to check if id exists / insertOrReplace
}

//object DBVersions : IntIdTable() {
//    val version = integer("version").default(0)
//}
//
//class DBVersion(id: EntityID<Int>) : IntEntity(id) {
//    companion object : IntEntityClass<DBVersion>(DBVersions)
//
//    var version by DBVersions.version
//}