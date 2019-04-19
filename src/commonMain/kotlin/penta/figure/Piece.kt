package penta.figure

import io.data2viz.geom.Point
import penta.PentaColor

abstract class Piece {
    abstract val id: String
    abstract val color: PentaColor
    abstract var pos: Point
    abstract val radius: Double

}
