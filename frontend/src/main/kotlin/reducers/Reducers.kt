package reducers

import actions.Action
import initialState
import penta.PentaMove
import penta.redux_rewrite.BoardState
import penta.redux_rewrite.BoardState.Companion.processMove
import redux.RAction
import util.combineReducers
import penta.ConnectionState
import penta.SerialNotation
import penta.redux.MultiplayerState

data class State(
    val boardState: BoardState = BoardState.create(),
    val multiplayerState: MultiplayerState = MultiplayerState()
//    val array: Array<String> = emptyArray()
) {
    companion object{
        fun combinedReducers(): (State, action: RAction) -> State = combineReducers(
            mapOf(
                State::boardState to ::boardState,
                State::multiplayerState to ::multiplayerState
            )
        )

        fun boardState(state: BoardState = initialState.boardState, action: Any): BoardState {
            return when(action) {
                is Action<*> -> when(val wrappedAction = action.action) {
                    is PentaMove -> {
                        BoardState.Companion.WithMutableState(state).processMove(wrappedAction)
                    }
                    is SerialNotation -> {
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
                is SerialNotation -> {
                    val move = action.asMove(state)
                    BoardState.Companion.WithMutableState(state).processMove(move)
                }
                is BoardState -> {
                    action
                }
                else -> state
            }
        }
        fun multiplayerState(inputState: MultiplayerState = MultiplayerState(), action: Any): MultiplayerState {
            return when(action) {
                is ConnectionState -> {
                    inputState.copy(
                        connectionState = action
                    )
                }
                is MultiplayerState.Companion.Actions -> {
                    MultiplayerState.reducer(inputState, action)
                }
                else -> inputState
            }
        }
    }
}


