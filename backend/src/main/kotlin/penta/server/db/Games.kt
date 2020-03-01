package penta.server.db

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Table
import java.util.UUID

object Games : UUIDTable() {
    val gameId = varchar("gameId", 50)
        .uniqueIndex("gameId")
    val history = jsonb2("history")//, json, GameEvent.serializer().list)
    val owner = reference("owner", Users)
    init {
        gameId.defaultValueFun = { "game_$id" }
    }
}

class Game(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Game>(Games)

    var gameId by Games.gameId
    var history by Games.history
    var owner by User referencedOn Games.owner
    var playingUsers by PlayingUser via UserInGames
}

object UserInGames: Table("user_in_game") {
    val gameId = reference("game", Games)
    val playerInGame = reference("playerInGame", PlayingUsers)
    override val primaryKey: PrimaryKey = PrimaryKey(gameId, playerInGame)
}

object PlayingUsers: UUIDTable("playingUsers") {
    val gameId = reference("game", Games)//, onDelete = ReferenceOption.CASCADE)
    val userId = reference("user", Users)//, onDelete = ReferenceOption.CASCADE)
    val player = varchar("player", 20)
    val shape = varchar("shape", 20)
    init {
        index(true, gameId, player)
    }
}

class PlayingUser(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<PlayingUser>(PlayingUsers)
    var game by Game referencedOn PlayingUsers.gameId
    var user by User referencedOn PlayingUsers.userId
    var player by PlayingUsers.player
    var shape by PlayingUsers.shape
}
