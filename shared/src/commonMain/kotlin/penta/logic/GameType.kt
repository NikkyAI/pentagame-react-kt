package penta.logic

import penta.PlayerState

enum class GameType(val teams: Map<Int, List<PlayerState>>) {
    TWO(mapOf(
        1 to listOf(PlayerState.PLAYER_1),
        2 to listOf(PlayerState.PLAYER_2)
    )),
    THREE(mapOf(
        1 to listOf(PlayerState.PLAYER_1),
        2 to listOf(PlayerState.PLAYER_2),
        3 to listOf(PlayerState.PLAYER_3)
    )),
    FOUR(mapOf(
        1 to listOf(PlayerState.PLAYER_1),
        2 to listOf(PlayerState.PLAYER_2),
        3 to listOf(PlayerState.PLAYER_3),
        4 to listOf(PlayerState.PLAYER_4)
    )),
    TWO_VS_TO(mapOf(
        1 to listOf(PlayerState.PLAYER_1, PlayerState.PLAYER_3),
        2 to listOf(PlayerState.PLAYER_2, PlayerState.PLAYER_4)
    ));
    val playerCount: Int = teams.values.flatten().size
    val players: List<PlayerState> = teams.values.flatten().sorted()
}

// TODO: replace with data class ?
// example:
// playerCount: 3
// teams: [[1], [2], [3]]