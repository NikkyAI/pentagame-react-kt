package com.bdudelsack.fullstack

import com.bdudelsack.fullstack.store.Store
import com.bdudelsack.fullstack.store.storeProvider
import com.bdudelsack.fullstack.ui.todoList
import kotlinx.css.Color
import kotlinx.css.backgroundColor
import kotlinx.css.padding
import kotlinx.css.px
import react.dom.h1
import react.dom.render
import styled.css
import styled.styledDiv
import styled.styledH1
import kotlin.browser.document


fun main() {
    val store = Store(
        todos = listOf(
            TodoItem("0", "This is the first todo item"),
            TodoItem("1", "This is the second todo item")
        )
    )

    render(document.getElementById("container")) {
        storeProvider(store) {
            h1 {
                +"Kotlin fullstack example !!!"
            }

            styledDiv {
                css {
                    padding(vertical = 16.px)
                    backgroundColor = Color.green
                }
                +"this uses kotlin-styled"
            }

            todoList()
        }
    }
}
