package penta.logic

import io.data2viz.color.Color
import io.data2viz.color.Colors
import io.data2viz.geom.Point
import penta.PentaColor
import penta.logic.field.JointField

sealed class Piece {
    abstract val id: String
    abstract val pentaColor: PentaColor
    abstract var pos: Point
    abstract val radius: Double

    open val color: Color get() = pentaColor.color

    interface Blocker

    data class BlackBlocker(
        override val id: String,
        override var pos: Point,
        override val radius: Double,
        override val pentaColor: PentaColor,
        val originalPosition: JointField
    ): Piece(), Blocker {

        override val color: Color = Colors.Web.black
    }

    data class GrayBlocker(
        override val id: String,
        override var pos: Point,
        override val radius: Double,
        override val pentaColor: PentaColor
    ): Piece(), Blocker {
        override val color: Color = Colors.Web.gray
    }

    data class Player(
        override val id: String,
        val playerId: String,
        val figureId: String,
        override var pos: Point,
        override val radius: Double,
        override val pentaColor: PentaColor
    ): Piece() {

        override val color: Color get() = pentaColor.color.brighten(1.0)
    }
}
