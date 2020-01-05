package com.bdudelsack.fullstack

import WrapperStore
import com.bdudelsack.fullstack.store.Store
import com.bdudelsack.fullstack.store.storeProvider
import com.bdudelsack.fullstack.ui.todoList
import kotlinx.css.Color
import kotlinx.css.backgroundColor
import kotlinx.css.padding
import kotlinx.css.px
import mu.KotlinLogging
import org.reduxkotlin.applyMiddleware
import org.reduxkotlin.createStore
import penta.PentaMove
import penta.PlayerState
import penta.redux_rewrite.BoardState
import react.dom.h1
import react.dom.render
import styled.css
import styled.styledDiv
import styled.styledH1
import kotlin.browser.document


private val logger = KotlinLogging.logger {}
fun main() {
    val boardStore: org.reduxkotlin.Store<BoardState> = createStore(
        BoardState.reducer,
        BoardState.create(
            // TODO: remove default users
            listOf(PlayerState("alice", "cross"), PlayerState("bob", "triangle")),
            BoardState.GameType.TWO
        ),
        applyMiddleware(/*loggingMiddleware(logger)*/)
    )
    logger.info { "initialized" }
    boardStore.dispatch(PentaMove.PlayerJoin(PlayerState("eve", "square")))

//    val wrapper: redux.Store<BoardState, PentaMove, BoardState> = WrapperStore(boardStore)
    val wrapper = WrapperStore<BoardState, PentaMove>(boardStore)
    logger.info { "initialized" }
    wrapper.dispatch(PentaMove.InitGame)

//    val store = Store(
//        todos = listOf(
//            TodoItem("0", "This is the first todo item"),
//            TodoItem("1", "This is the second todo item")
//        )
//    )
//
//    render(document.getElementById("container")) {
//        storeProvider(store) {
//            h1 {
//                +"Kotlin fullstack example !!!"
//            }
//
//            styledDiv {
//                css {
//                    padding(vertical = 16.px)
//                    backgroundColor = Color.green
//                }
//                +"this uses kotlin-styled"
//            }
//
//            todoList()
//        }
//    }
}
