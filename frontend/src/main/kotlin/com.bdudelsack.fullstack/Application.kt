package com.bdudelsack.fullstack

import com.bdudelsack.fullstack.store.Store
import com.bdudelsack.fullstack.store.storeProvider
import com.bdudelsack.fullstack.ui.todoList
import react.dom.h1
import react.dom.render
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

            todoList()
        }
    }
}
