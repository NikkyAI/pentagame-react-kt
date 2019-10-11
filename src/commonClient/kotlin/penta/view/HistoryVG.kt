package penta.view

import com.lightningkite.reacktive.property.ConstantObservableProperty
import com.lightningkite.reacktive.property.StandardObservableProperty
import com.lightningkite.koolui.concepts.TextSize
import com.lightningkite.koolui.geometry.AlignPair
import com.lightningkite.koolui.views.basic.*
import com.lightningkite.koolui.image.MaterialIcon
import com.lightningkite.koolui.views.navigation.pagesEmbedded
import com.lightningkite.reacktive.property.TransformObservableProperty
import penta.view.MyViewFactory
import penta.view.MyViewGenerator
import com.lightningkite.reacktive.property.transform

class HistoryVG<VIEW>() : MyViewGenerator<VIEW> {
    override val title: String = "History"

    override fun generate(dependency: MyViewFactory<VIEW>): VIEW = with(dependency) {
        text(
            text = PentaViz.gameState.history.onListUpdate.transform { list ->
                list.joinToString("\n") {
                    it.asNotation()
                }
            },
            align = AlignPair.TopLeft
        )
    }
}
