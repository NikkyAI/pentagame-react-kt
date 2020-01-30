package penta.redux

import penta.ConnectionState
import penta.LobbyState
import penta.network.GameSessionInfo
import penta.util.exhaustive

data class MultiplayerState(
    val gameObservers: List<String> = listOf(),
    val connectionState: ConnectionState = ConnectionState.Disconnected(),
    val lobby: LobbyState = LobbyState()
) {
    fun reduce(action: Actions): MultiplayerState = when(action) {
        is Actions.AddObserver -> copy(
            gameObservers = gameObservers + action.observer
        )
        is Actions.RemoveObserver -> copy(
            gameObservers = gameObservers - action.observer
        )
        is Actions.SetGames -> copy(
            lobby = lobby.copy(games = action.games.associateBy { it.id })
        )
        is Actions.SetConnectionState -> copy(
            connectionState = action.connectionState
        )
    }.exhaustive

//    fun reduceLobby(action: LobbyEvent): MultiplayerState  {
//        return copy(
//            lobby = lobby.reduce(action)
//        )
//    }

    companion object {
        sealed class Actions {
            data class AddObserver(val observer: String): Actions()
            data class RemoveObserver(val observer: String): Actions()
            data class SetGames(val games: List<GameSessionInfo>): Actions()
            data class SetConnectionState(val connectionState: ConnectionState): Actions()
        }
    }
}