package com.bdudelsack.fullstack

import kotlinx.serialization.Serializable

@Serializable
data class TodoItem(
    val id: String,
    val description: String,
    val completed: Boolean = false
)
