package penta.figure

import io.data2viz.color.Color
import io.data2viz.color.Colors
import io.data2viz.geom.Point
import penta.PentaColor

data class BlackBlockerPiece(
    override val id: String,
    override var pos: Point,
    override val radius: Double,
    override val pentaColor: PentaColor
): BlockerPiece() {

    override val color: Color = Colors.Web.black
}
