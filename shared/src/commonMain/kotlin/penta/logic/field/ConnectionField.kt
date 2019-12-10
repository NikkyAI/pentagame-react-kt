package penta.logic.field

import PentaMath
import io.data2viz.color.Color
import io.data2viz.geom.Point

data class ConnectionField(
    override val id: String,
    val altId: String,
    override val pos: Point,
    override val color: Color
) : AbstractField() {
    override val radius: Double = PentaMath.s / 2
}