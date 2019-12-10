package com.bdudelsack.fullstack.store

import com.bdudelsack.fullstack.TodoItem

data class Store(
    val todos: List<TodoItem> = listOf()
)
