package penta

import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    val id: String,
    var figureId: String
)