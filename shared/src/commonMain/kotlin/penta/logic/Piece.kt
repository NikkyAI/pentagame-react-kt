package penta.logic

import io.data2viz.color.Color
import penta.PentaColor
import penta.PentaColors
import penta.logic.Field.Goal

sealed class Piece {
    abstract val id: String
    abstract val pentaColor: PentaColor
    abstract val radius: Double

    open val color: Color get() = pentaColor.color

    interface Blocker {
        companion object {
            const val RADIUS = PentaMath.s / 2.5
        }

    }

    data class BlackBlocker(
        override val id: String,
        override val pentaColor: PentaColor,
        val originalPosition: Goal
    ) : Piece(), Blocker {
        override val radius get() = Blocker.RADIUS
        override val color: Color get() = PentaColors.BLACK
    }

    data class GrayBlocker(
        override val id: String,
        override val pentaColor: PentaColor
    ) : Piece(), Blocker {
        override val radius get() = Blocker.RADIUS
        override val color: Color get() = PentaColors.GREY
    }

    data class Player(
        override val id: String,
        val playerId: String,
        val figureId: String,
        override val pentaColor: PentaColor
    ) : Piece() {
        override val radius: Double get() = RADIUS
        override val color: Color get() = pentaColor.color.brighten(0.5)
        companion object {
            const val RADIUS = PentaMath.s / 2.3
        }
    }
}
