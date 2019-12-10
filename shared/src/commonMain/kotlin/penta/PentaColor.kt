package penta

import com.lightningkite.reacktive.property.MutableObservableProperty
import com.lightningkite.reacktive.property.ObservableProperty
import com.lightningkite.reacktive.property.StandardObservableProperty
import io.data2viz.color.Color
import io.data2viz.color.Colors
import io.data2viz.color.col

enum class PentaColor(val label: String, val color: MutableObservableProperty<Color>) { //}, val root: penta.math.Point) {
    A("white", StandardObservableProperty(0xf6f6f6.col)),//, penta.math.Point(+1.0, 0.0)), #f6f6f6
    B("blue", StandardObservableProperty(Colors.Web.dodgerblue)),//, penta.math.Point(+1.0, +1.0)),
    C("red", StandardObservableProperty(Colors.Web.red)),//, penta.math.Point(-1.0, +1.0)),
    D("yellow", StandardObservableProperty(Colors.Web.yellow)),//, penta.math.Point(-1.0, -1.0)),
    E("green", StandardObservableProperty(Colors.Web.forestgreen))//, penta.math.Point(+1.0, -1.0));
}

object PentaColors {
    val BLACK: MutableObservableProperty<Color> = StandardObservableProperty(Colors.Web.black)
    val GREY: MutableObservableProperty<Color> = StandardObservableProperty(Colors.Web.grey)
}