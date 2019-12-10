package com.bdudelsack.fullstack.ui

import com.bdudelsack.fullstack.store.useStore
import react.RBuilder
import react.RProps
import react.child
import react.dom.div
import react.functionalComponent

val todoList = functionalComponent<RProps> {
    val store = useStore()

    div {
        for(todo in store.todos) {
            div {
                + "#"
                + todo.id
                + " - "
                + todo.description
            }
        }
    }
}

fun RBuilder.todoList() = child(todoList)
