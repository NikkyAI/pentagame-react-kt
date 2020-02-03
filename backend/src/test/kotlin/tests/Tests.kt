package tests

import actions.Action
import com.soywiz.klogger.Logger
import org.junit.Test
import org.reduxkotlin.applyMiddleware
import org.reduxkotlin.createStore
import penta.PentaMove
import penta.PlayerState
import penta.BoardState

class Tests {
    private val logger = Logger(this::class.simpleName!!)
    @Test
    fun test() {
        val boardStore: org.reduxkotlin.Store<BoardState> = createStore(
            BoardState.Companion::reduceFunc,
            BoardState.create(),
            applyMiddleware(/*loggingMiddleware(logger)*/)
        )
        logger.info { "initialized" }
        boardStore.dispatch(Action(PentaMove.PlayerJoin(PlayerState("eve", "square"))))
        boardStore.dispatch(Action(PentaMove.InitGame))
    }

}