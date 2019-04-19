package penta.figure

import io.data2viz.geom.Point
import penta.PentaColor

data class BlockerPiece(
    override val id: String,
    val blockerType: BlockerType,
    override var pos: Point,
    override val radius: Double,
    override val color: PentaColor
): Piece()
