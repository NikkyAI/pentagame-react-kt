package penta.network

import kotlinx.serialization.Serializable
import penta.PlayerState
import penta.UserInfo

@Serializable
data class GameSessionInfo(
    val id: String,
    val owner: String,
    val running: Boolean,
    val playingUsers: Map<PlayerState, UserInfo>,
    val observers: List<String>
)