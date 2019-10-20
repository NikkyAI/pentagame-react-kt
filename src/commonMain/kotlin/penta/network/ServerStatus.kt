package penta.network

import kotlinx.serialization.Serializable

@Serializable
data class ServerStatus(
    val totalPlayers: Int
)