package penta.network

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest (
    val userId: String,
    val password: String? = null
)