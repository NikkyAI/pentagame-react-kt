package penta.field

import io.data2viz.color.Color
import io.data2viz.geom.Point

abstract class AbstractField {
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
