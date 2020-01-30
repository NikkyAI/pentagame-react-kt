package penta.redux

import penta.ConnectionState
import penta.WSClient
import penta.network.GameSessionInfo
import penta.network.LobbyEvent
import penta.util.exhaustive

data class MultiplayerState(
    val gameObservers: List<String> = listOf(),
    val connectionState: ConnectionState = ConnectionState.Disconnected(),
    val lobbyUsers: List<String> = listOf(), // TODO: use this ?
    val chatHistory: List<LobbyEvent.Message> = listOf(),
    val games: Map<String, GameSessionInfo> = mapOf()
) {
    fun reduce(action: Actions): MultiplayerState = when(action) {
        is Actions.AddObserver -> copy(
            gameObservers = gameObservers + action.observer
        )
        is Actions.RemoveObserver -> copy(
            gameObservers = gameObservers - action.observer
        )
        is Actions.SetGames -> copy(
            games = action.games.associateBy { it.id }
        )
        is Actions.SetConnectionState -> copy(
            connectionState = action.connectionState
        )
    }.exhaustive

    fun reduceLobby(action: LobbyEvent): MultiplayerState = when(action) {
        is LobbyEvent.InitialSync -> copy(
            lobbyUsers = action.users,
            chatHistory = action.chat,
            games = action.games.associateBy { it.id }
        )
        is LobbyEvent.UpdateGame -> copy(
            games = games + (action.game.id to action.game)
        )
        is LobbyEvent.Message -> TODO()
        is LobbyEvent.Join -> TODO()
        is LobbyEvent.Leave -> TODO()
        else -> {
//            logger.error { "unhandled event: $action" }
            throw IllegalStateException("unhandled event: $action")
        }
    }

    companion object {
        sealed class Actions {
            data class AddObserver(val observer: String): Actions()
            data class RemoveObserver(val observer: String): Actions()
            data class SetGames(val games: List<GameSessionInfo>): Actions()
            data class SetConnectionState(val connectionState: ConnectionState): Actions()
        }
    }
}