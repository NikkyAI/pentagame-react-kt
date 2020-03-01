package penta.network

import kotlinx.serialization.Serializable
import penta.PlayerIds
import penta.UserInfo

@Serializable
data class GameSessionInfo(
    val id: String,
    val owner: String,
    val running: Boolean,
    val playingUsers: Map<PlayerIds, UserInfo>,
    val observers: List<String>
)