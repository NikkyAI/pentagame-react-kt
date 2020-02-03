package reducers

import actions.Action
import com.soywiz.klogger.Logger
import io.ktor.http.Url
import penta.BoardState
import penta.BoardState.Companion.processMove
import penta.ConnectionState
import penta.PentaMove
import penta.network.GameEvent
import penta.network.LobbyEvent
import penta.redux.MultiplayerState
import penta.util.exhaustive
import kotlin.browser.document
import kotlin.browser.window

data class State(
    val boardState: BoardState = BoardState.create(),
    val multiplayerState: MultiplayerState = MultiplayerState(
        connectionState = ConnectionState.Disconnected(baseUrl = Url(document.location!!.href))
    )
//    val array: Array<String> = emptyArray()
) {
    fun reduce(action: Any): State {
        return when (action) {
            is Action<*> -> {
                reduce(action.action)
            }
            is PentaMove -> {
                copy(boardState = BoardState.Companion.WithMutableState(boardState).processMove(action))
            }
            is GameEvent -> {
                val move = action.asMove(boardState)
                copy(boardState = BoardState.Companion.WithMutableState(boardState).processMove(move))
            }
            is BoardState -> {
                // TODO: split into actions
                console.warn("should not copy state from action")
                copy(boardState = action)
            }
            is MultiplayerState.Companion.Actions -> {
                copy(multiplayerState = multiplayerState.reduce(action))
            }
            is LobbyEvent -> {
                copy(
                    multiplayerState = with(multiplayerState) {
                        copy(
                            lobby = lobby.reduce(action)
                        )
                    }
                )
            }
            is ConnectionState -> {
                copy(
                    multiplayerState = multiplayerState.copy(
                        connectionState = action
                    )
                )
            }
            else -> {
                this
            }
        }.exhaustive
    }

    companion object {
        private val logger = Logger(this::class.simpleName!!)
        val reducer: (State, Any) -> State = State::reduce
    }
}


