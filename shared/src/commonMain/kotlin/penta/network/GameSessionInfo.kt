package penta.network

import kotlinx.serialization.Serializable

@Serializable
data class GameSessionInfo(
    val id: String,
    val owner: String,
    val running: Boolean,
    val players: List<String>,
    val observers: List<String>
)