package penta.redux

import org.reduxkotlin.Reducer
import penta.ConnectionState
import penta.util.exhaustive

data class MultiplayerState(
    val observers: List<String> = listOf(),
    val connectionState: ConnectionState = ConnectionState.Disconnected()
) {
    companion object {
        sealed class Actions {
            data class AddObserver(val observer: String): Actions()
            data class RemoveObserver(val observer: String): Actions()
            data class SetConnectionState(val connectionState: ConnectionState): Actions()
        }

        val reducer: Reducer<MultiplayerState> = { state, action ->
            action as Actions
            when(action) {
                is Actions.AddObserver -> {
                    state.copy(observers = state.observers + action.observer)
                }
                is Actions.RemoveObserver -> {
                    state.copy(observers = state.observers - action.observer)
                }
                is Actions.SetConnectionState -> {
                    state.copy(connectionState = action.connectionState)
                }
            }.exhaustive
        }
    }
}