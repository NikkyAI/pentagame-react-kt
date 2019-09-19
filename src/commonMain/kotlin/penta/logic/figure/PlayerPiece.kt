package penta.logic.figure

import io.data2viz.color.Color
import io.data2viz.geom.Point
import penta.PentaColor

data class PlayerPiece(
    override val id: String,
    val playerId: String,
    override var pos: Point,
    override val radius: Double,
    override val pentaColor: PentaColor
): Piece() {

    override val color: Color get() = pentaColor.color.brighten(1.0)
}
