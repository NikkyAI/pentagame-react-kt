package reducers

import actions.Action
import initialState
import penta.PentaMove
import penta.redux_rewrite.BoardState
import penta.redux_rewrite.BoardState.Companion.processMove
import redux.RAction
import util.combineReducers
import penta.ConnectionState
import penta.network.GameEvent

data class State(
    val boardState: BoardState = BoardState.create(),
    val connection: ConnectionState = ConnectionState.Disconnected()
//    val array: Array<String> = emptyArray()
) {
    companion object{
        fun combinedReducers(): (State, action: RAction) -> State = combineReducers(
            mapOf(
                State::boardState to ::boardState,
                State::connection to ::connection
            )
        )

        fun boardState(state: BoardState = initialState.boardState, action: Any): BoardState {
            return when(action) {
                is Action<*> -> when(val wrappedAction = action.action) {
                    is PentaMove -> {
                        BoardState.Companion.WithMutableState(state).processMove(wrappedAction)
                    }
                    is GameEvent -> {
                        val move = wrappedAction.asMove(state)
                        BoardState.Companion.WithMutableState(state).processMove(move)
                    }
                    is BoardState -> {
                        wrappedAction
                    }
                    else -> state
                }
                is PentaMove -> {
                    BoardState.Companion.WithMutableState(state).processMove(action)
                }
                is GameEvent -> {
                    val move = action.asMove(state)
                    BoardState.Companion.WithMutableState(state).processMove(move)
                }
                is BoardState -> {
                    action
                }
                else -> state
            }
        }
        fun connection(inputState: ConnectionState = ConnectionState.Disconnected(), action: Any): ConnectionState {
            return when(action) {
                is Action<*> -> when(val connectionState = action.action) {
                    is ConnectionState -> {
                        connectionState
                    }
                    else -> inputState
                }
                is ConnectionState -> action
                else -> inputState
            }
        }
    }
}


