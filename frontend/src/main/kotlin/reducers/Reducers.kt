package reducers

import actions.Action
import mu.KotlinLogging
import penta.PentaMove
import penta.redux_rewrite.BoardState
import penta.redux_rewrite.BoardState.Companion.processMove
import redux.RAction
import util.combineReducers

data class State(
    val boardState: BoardState = BoardState(),
    val array: Array<String> = emptyArray()
) {
    companion object{
        val logger = KotlinLogging.logger {}

        fun combinedReducers() = combineReducers(
            mapOf(
                State::boardState to ::boardState,
                State::array to ::array
            )
        )

        fun boardState(state: BoardState = BoardState(), action: RAction): BoardState {
            logger.info { "state: $state" }
            logger.info { "action: $action" }
            return when(action) {
                is Action<*> -> {
                    BoardState.Companion.WithMutableState(state).processMove(action.action as PentaMove)
                }
                else -> state
            }
        }
        fun array(state: Array<String> = emptyArray(), action: RAction): Array<String> {
            val logger = KotlinLogging.logger {}
            logger.info { "state: $state" }
            logger.info { "action: $action" }
            return when(action) {
//        is Action<*> -> {
//            // do stuff with array
//        }
                else -> state
            }
        }
    }
}


