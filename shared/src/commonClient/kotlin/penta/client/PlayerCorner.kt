package penta.client

import io.data2viz.viz.CircleNode
import io.data2viz.viz.PathNode
import penta.PlayerState

data class PlayerCorner(
    val player: PlayerState,
    val face: PathNode,
    val graySlot: CircleNode
) {
    fun update() {
    }
}