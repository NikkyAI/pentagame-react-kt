package penta

import io.data2viz.color.Color
import io.data2viz.color.Colors
import io.data2viz.color.col

enum class PentaColor(val label: String, val color: Color) { //}, val root: penta.math.Point) {
    A("white", 0xf6f6f6.col),//, penta.math.Point(+1.0, 0.0)), #f6f6f6
    B("blue", Colors.Web.dodgerblue),//, penta.math.Point(+1.0, +1.0)),
    C("red", Colors.Web.red),//, penta.math.Point(-1.0, +1.0)),
    D("yellow", Colors.Web.yellow),//, penta.math.Point(-1.0, -1.0)),
    E("green", Colors.Web.forestgreen)//, penta.math.Point(+1.0, -1.0));
}

object PentaColors {
    val FOREGROUND: Color = "#d3d3d3".col
    val BACKGROUND: Color = "#28292b".col
    val BLACK: Color = Colors.Web.black
    val GREY: Color = Colors.Web.grey
}