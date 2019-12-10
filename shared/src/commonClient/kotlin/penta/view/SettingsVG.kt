package penta.view

import com.lightningkite.koolui.views.basic.text
import com.lightningkite.koolui.views.layout.horizontal
import com.lightningkite.koolui.views.layout.vertical
import com.lightningkite.reacktive.property.MutableObservableProperty
import com.lightningkite.reacktive.property.transform
import io.data2viz.color.col
import penta.PentaColor
import penta.PentaColors

class SettingsVG<VIEW>() : MyViewGenerator<VIEW> {
    override val title: String = "Settings"

    override fun generate(dependency: MyViewFactory<VIEW>): VIEW = with(dependency) {
        fun colorSetter(color: MutableObservableProperty<io.data2viz.color.Color>) = horizontal {
            val rgbString = color.transform(
                {c -> c.rgbHex},
                {s -> s.col}
            )
//            val rgbString = StandardObservableProperty(color.value.rgbHex)
            -textField(rgbString)
        }
        vertical {
            -text("A")
            -colorSetter(PentaColor.A.color)
            -text("B")
            -colorSetter(PentaColor.B.color)
            -text("C")
            -colorSetter(PentaColor.C.color)
            -text("D")
            -colorSetter(PentaColor.D.color)
            -text("E")
            -colorSetter(PentaColor.E.color)
            -text("BLACK")
            -colorSetter(PentaColors.BLACK)
            -text("GREY")
            -colorSetter(PentaColors.GREY)
        }
    }
}
