package reducers

import actions.Action
import mu.KotlinLogging
import penta.PentaMove
import penta.redux_rewrite.BoardState
import penta.redux_rewrite.BoardState.Companion.processMove
import redux.RAction
import util.combineReducers
import penta.ConnectionState

data class State(
    val boardState: BoardState = BoardState.create(),
    val connection: ConnectionState = ConnectionState.Disconnected()
//    val array: Array<String> = emptyArray()
) {
    companion object{
        val logger = KotlinLogging.logger {}

        fun combinedReducers() = combineReducers(
            mapOf(
                State::boardState to ::boardState,
                State::connection to ::connection
            )
        )

        fun boardState(state: BoardState = BoardState.create(), action: RAction): BoardState {
            console.log("state: $state")
            console.log("action: $action")
            if(action != undefined) {
                console.log("action.js: ${action::class.js}")
            }
            return when(action) {
                is Action<*> -> when(val pentaMove = action.action) {
                    is PentaMove -> {
                        BoardState.Companion.WithMutableState(state).processMove(pentaMove)
                    }
                    else -> state
                }
                else -> state
            }
        }
        fun connection(state: ConnectionState = ConnectionState.Disconnected(), action: RAction): ConnectionState {
            console.log("state: $state")
            console.log("action: $action")
            return when(action) {
//        is Action<*> -> {
//            // do stuff with array
//        }
                else -> state
            }
        }
    }
}


