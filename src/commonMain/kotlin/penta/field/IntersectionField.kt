package penta.field

import io.data2viz.color.Color
import io.data2viz.geom.Point

abstract class IntersectionField: AbstractField() {
    override val radius: Double = PentaMath.c / 2

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