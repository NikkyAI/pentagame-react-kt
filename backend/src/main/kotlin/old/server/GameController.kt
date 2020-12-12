package old.server

import com.soywiz.klogger.Logger

object GameController {
    private val logger = Logger(this::class.simpleName!!)
    var idCounter = 0

    suspend fun create(owner: User): ServerGamestate {
        logger.info { "creating game for $owner" }
        val game = ServerGamestate("game_${idCounter++}", owner)
        GlobalState.dispatch(GlobalState.GlobalAction.AddGame(game))
        return game
    }

    suspend fun get(gameId: String): ServerGamestate? {
        return GlobalState.getState().games.find { it.serverGameId == gameId }
    }

    suspend fun listActiveGames() = GlobalState.getState().games.filter {
        it.getBoardState().winner == null
    }
}