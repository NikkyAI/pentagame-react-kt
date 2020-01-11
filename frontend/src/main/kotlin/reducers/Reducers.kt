package reducers

import actions.Action
import mu.KotlinLogging
import penta.PentaMove
import penta.redux_rewrite.BoardState
import penta.redux_rewrite.BoardState.Companion.processMove
import redux.RAction
import util.combineReducers

data class State(
    val boardState: BoardState = BoardState.create()
//    val array: Array<String> = emptyArray()
) {
    companion object{
        val logger = KotlinLogging.logger {}

        fun combinedReducers() = combineReducers(
            mapOf(
                State::boardState to ::boardState
//                State::array to ::array
            )
        )

        fun boardState(state: BoardState = BoardState.create(), action: RAction): BoardState {
            console.log("state: $state")
            console.log("action: $action")
            if(action != undefined) {
                console.log("action.js: ${action::class.js}")
            }
            return when(action) {
                is Action<*> -> {
                    BoardState.Companion.WithMutableState(state).processMove(action.action as PentaMove)
                }
                else -> state
            }
        }
        fun array(state: Array<String> = emptyArray(), action: RAction): Array<String> {
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


