package penta.server

import com.soywiz.klogger.Logger
import penta.network.LobbyEvent

object GameController {
    private val logger = Logger(this::class.simpleName!!)
    var idCounter = 0
    // TODO: convert to redux state
    val games = mutableListOf<ServerGamestate>()

    fun create(owner: User): ServerGamestate {
        logger.info { "creating game for $owner" }
        val game = ServerGamestate("game_${idCounter++}", owner)
        games += game
        LobbyHandler.store.dispatch(LobbyEvent.UpdateGame(game.info))
        return game
    }

    fun get(gameId: String): ServerGamestate? {
        return games.find { it.id == gameId }
    }
}