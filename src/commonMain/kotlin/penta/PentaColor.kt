package penta

import io.data2viz.color.Color
import io.data2viz.color.Colors
import io.data2viz.color.RgbColor
import io.data2viz.color.col
import io.data2viz.scale.ScalesChromatic

enum class PentaColor(val label: String, val color: Color) { //}, val root: penta.math.Point) {
    A("white", Colors.Web.deeppink),//, penta.math.Point(+1.0, 0.0)),
    B("blue", Colors.Web.dodgerblue),//, penta.math.Point(+1.0, +1.0)),
    C("red", Colors.Web.red),//, penta.math.Point(-1.0, +1.0)),
    D("yellow", Colors.Web.yellow),//, penta.math.Point(-1.0, -1.0)),
    E("green", Colors.Web.forestgreen)//, penta.math.Point(+1.0, -1.0));
}