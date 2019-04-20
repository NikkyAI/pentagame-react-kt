package penta.field

import io.data2viz.color.Color
import penta.PentaColor

abstract class IntersectionField: AbstractField() {
    abstract  val pentaColor: PentaColor
    override val radius: Double = PentaMath.c / 2

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