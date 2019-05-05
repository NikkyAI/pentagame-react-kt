package penta.figure

import io.data2viz.color.Color
import io.data2viz.geom.Point
import penta.PentaColor

abstract class Piece {
    abstract val id: String
    abstract val pentaColor: PentaColor
    abstract var pos: Point
    abstract val radius: Double

    open val color: Color get() = pentaColor.color
}
