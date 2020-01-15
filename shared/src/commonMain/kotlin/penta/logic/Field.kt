package penta.logic

import io.data2viz.color.Color
import io.data2viz.geom.Point
import penta.PentaColor
import penta.PentaColors

sealed class Field {
    abstract val id: String
    abstract val pos: Point
    abstract val radius: Double
    abstract val color: Color

    protected var connectedFields: List<Field> = listOf()
    fun connect(vararg others: Field) {
        connectedFields += others
        others.forEach {
            it.connectedFields += this
        }
    }

    open val connected: List<Field>
        get() = connectedFields

    abstract class Intersection : Field() {
        abstract val pentaColor: PentaColor

        override val color: Color get() = pentaColor.color

        protected var connectedIntersectionFields: List<Intersection> = listOf()
        fun connectIntersection(vararg others: Intersection) {
            connectedIntersectionFields += others
            others.forEach {
                it.connectedIntersectionFields += this
            }
        }

        open val connectedIntersections: List<Intersection>
            get() = connectedIntersectionFields
    }
    data class ConnectionField(
        override val id: String,
        val altId: String,
        override val pos: Point
    ) : Field() {
        override val color: Color = PentaColors.FOREGROUND
        override val radius: Double = PentaMath.s / 2
    }

    data class Goal(
        override val id: String,
        override val pos: Point,
        override val pentaColor: PentaColor
    ) : Intersection() {
        override val radius: Double = PentaMath.j / 2
        override val connected: List<Field>
            get() = connectedFields
    }


    data class Start(
        override val id: String,
        override val pos: Point,
        override val pentaColor: PentaColor
    ) : Intersection() {
        override val radius: Double = PentaMath.c / 2
        override val connected: List<Field>
            get() = connectedFields
    }

}
