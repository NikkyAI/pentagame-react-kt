package penta.server.db

import kotlinx.serialization.list
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import penta.network.GameEvent
import penta.util.json
import java.util.UUID

object Games : UUIDTable() {
    val gameId = varchar("gameId", 50)
        .uniqueIndex("gameId")
    val history = jsonb2("history")//, json, GameEvent.serializer().list)

    init {
        gameId.defaultValueFun = { "game_$id" }
    }
}

class Game(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Game>(Games)

    var gameId by Games.gameId
    var history by Games.history
    var players by User via PlayersInGames
}

object PlayersInGames: Table("players_in_games") {
    val playerId = reference("user", Users)//, onDelete = ReferenceOption.CASCADE)
    val gameId = reference("game", Games)//, onDelete = ReferenceOption.CASCADE)
    override val primaryKey: PrimaryKey = PrimaryKey(playerId, gameId)
}