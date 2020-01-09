package penta

import kotlinx.serialization.Serializable

@Serializable
data class PlayerState(
    val id: String,
    var figureId: String
)