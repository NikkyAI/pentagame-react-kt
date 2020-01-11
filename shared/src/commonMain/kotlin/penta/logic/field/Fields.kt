package penta.logic.field

import io.data2viz.color.Color
import io.data2viz.color.Colors
import io.data2viz.geom.Point
import penta.PentaColor
import penta.PentaColors

sealed class AbstractField {
    abstract val id: String
    abstract val pos: Point
    abstract val radius: Double
    abstract val color: Color

    protected var connectedFields: List<AbstractField> = listOf()
    fun connect(vararg others: AbstractField) {
        connectedFields += others
        others.forEach {
            it.connectedFields += this
        }
    }

    open val connected: List<AbstractField>
        get() = connectedFields
}

abstract class IntersectionField : AbstractField() {
    abstract val pentaColor: PentaColor

    override val color: Color get() = pentaColor.color

    protected var connectedIntersectionFields: List<IntersectionField> = listOf()
    fun connectIntersection(vararg others: IntersectionField) {
        connectedIntersectionFields += others
        others.forEach {
            it.connectedIntersectionFields += this
        }
    }

    open val connectedIntersections: List<IntersectionField>
        get() = connectedIntersectionFields
}
data class ConnectionField(
    override val id: String,
    val altId: String,
    override val pos: Point
) : AbstractField() {
    override val color: Color = PentaColors.FOREGROUND
    override val radius: Double = PentaMath.s / 2
}

data class GoalField(
    override val id: String,
    override val pos: Point,
    override val pentaColor: PentaColor
) : IntersectionField() {
    override val radius: Double = PentaMath.j / 2
    override val connected: List<AbstractField>
        get() = connectedFields
}


data class StartField(
    override val id: String,
    override val pos: Point,
    override val pentaColor: PentaColor
) : IntersectionField() {
    override val radius: Double = PentaMath.c / 2
    override val connected: List<AbstractField>
        get() = connectedFields

    val connectedGoalFields: List<GoalField> = listOf()
    val connectedStartFields: List<StartField> = listOf()
}
