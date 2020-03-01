package penta.logic

import penta.PlayerIds

enum class GameType(val teams: Map<Int, List<PlayerIds>>) {
    TWO(mapOf(
        1 to listOf(PlayerIds.PLAYER_1),
        2 to listOf(PlayerIds.PLAYER_2)
    )),
    THREE(mapOf(
        1 to listOf(PlayerIds.PLAYER_1),
        2 to listOf(PlayerIds.PLAYER_2),
        3 to listOf(PlayerIds.PLAYER_3)
    )),
    FOUR(mapOf(
        1 to listOf(PlayerIds.PLAYER_1),
        2 to listOf(PlayerIds.PLAYER_2),
        3 to listOf(PlayerIds.PLAYER_3),
        4 to listOf(PlayerIds.PLAYER_4)
    )),
    TWO_VS_TO(mapOf(
        1 to listOf(PlayerIds.PLAYER_1, PlayerIds.PLAYER_3),
        2 to listOf(PlayerIds.PLAYER_2, PlayerIds.PLAYER_4)
    ));
    val playerCount: Int = teams.values.flatten().size
    val players: List<PlayerIds> = teams.values.flatten().sortedBy { it.ordinal }
}

// TODO: replace with data class ?
// example:
// playerCount: 3
// teams: [[1], [2], [3]]