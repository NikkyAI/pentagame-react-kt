package penta

import com.soywiz.klogger.Logger
import penta.network.GameSessionInfo
import penta.network.LobbyEvent

data class LobbyState(
    val chat: List<LobbyEvent.Message> = listOf(),
    val users: List<String> = listOf(), // TODO: use this ?
    val games: Map<String, GameSessionInfo> = mapOf()
    // TODO: games: update from GameController
) {
    companion object {
        private val logger = Logger(this::class.simpleName!!)
    }
    fun reduce(action: LobbyEvent): LobbyState {
        logger.info { "action: $action" }
        return when(action) {
            is LobbyEvent.InitialSync -> copy(
                users = action.users,
                chat = action.chat,
                games = action.games
            )
            is LobbyEvent.UpdateGame -> copy(
                games = games + (action.game.id to action.game)
            )
            is LobbyEvent.Message -> copy(
                chat = chat + action
            )
            is LobbyEvent.Join -> copy(
                users = users + action.userId
            )
            is LobbyEvent.Leave -> copy(
                users = users - action.userId
            )
        }
    }
}
