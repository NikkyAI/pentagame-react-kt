package penta.logic.field

import PentaMath
import io.data2viz.geom.Point
import penta.PentaColor

data class CornerField(
    override val id: String,
    override val pos: Point,
    override val pentaColor: PentaColor
) : IntersectionField() {
    override val radius: Double = PentaMath.c / 2
    override val connected: List<AbstractField>
        get() = connectedFields

    val connectedJointFields: List<JointField> = listOf()
    val connectedCornerFields: List<CornerField> = listOf()
}