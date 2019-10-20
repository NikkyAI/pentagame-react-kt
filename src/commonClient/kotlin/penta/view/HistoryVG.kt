package penta.view

import com.lightningkite.koolui.concepts.Animation
import com.lightningkite.koolui.views.basic.*
import com.lightningkite.reacktive.property.transform
import com.lightningkite.koolui.views.layout.vertical

class HistoryVG<VIEW>() : MyViewGenerator<VIEW> {
    override val title: String = "History"

    override fun generate(dependency: MyViewFactory<VIEW>): VIEW = with(dependency) {
        swap(
            view = PentaViz.gameState.history.onListUpdate.transform { list ->
                scrollVertical(
                    vertical {
                        list.forEach { move ->
                            -text(
                                text = move.asNotation()
//                        align = AlignPair.TopLeft
                            )
                        }
                    }
                ) to Animation.None
            }
        )
    }
}
