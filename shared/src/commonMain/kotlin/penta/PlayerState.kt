package penta

import kotlinx.serialization.Serializable

@Serializable
// TODO: switch to enum ?
enum class PlayerState {
    PLAYER_1, PLAYER_2, PLAYER_3, PLAYER_4;

    val id: String
        get() = this.name
}