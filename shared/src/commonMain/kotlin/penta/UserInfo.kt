package penta

import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    val userId: String,
    var figureId: String // TODO: turn into enum or sealed class
)