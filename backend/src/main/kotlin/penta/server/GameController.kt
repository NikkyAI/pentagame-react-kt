package penta.server

import com.soywiz.klogger.Logger
import penta.server.GlobalState.Companion.store

object GameController {
    private val logger = Logger(this::class.simpleName!!)
    var idCounter = 0

    fun create(owner: User): ServerGamestate {
        logger.info { "creating game for $owner" }
        val game = ServerGamestate("game_${idCounter++}", owner)
        store.dispatch(GlobalState.GlobalAction.AddGame(game))
        return game
    }

    fun get(gameId: String): ServerGamestate? {
        return store.state.games.find { it.id == gameId }
    }
}