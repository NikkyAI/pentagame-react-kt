package penta

import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    val name: String,
    var figureId: String // TODO: turn into enum or sealed class
)