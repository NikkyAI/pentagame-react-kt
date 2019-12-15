package penta.view

import PentaViz
import com.lightningkite.koolui.concepts.Animation
import com.lightningkite.koolui.views.basic.text
import com.lightningkite.koolui.views.layout.vertical
import com.lightningkite.reacktive.property.StandardObservableProperty
import com.lightningkite.reacktive.property.transform

class HistoryVG<VIEW>() : MyViewGenerator<VIEW> {
    override val title: String = "History"

    override fun generate(dependency: MyViewFactory<VIEW>): VIEW = with(dependency) {
        text("add boardState.history")
//        swap(
////            view = PentaViz.gameState.history.onListUpdate.transform { list ->
////                scrollVertical(
////                    vertical {
////                        list.forEach { move ->
////                            -text(
////                                text = move.asNotation()
//////                        align = AlignPair.TopLeft
////                            )
////                        }
////                    },
////                    StandardObservableProperty(100f)
////                ) to Animation.None
////            }
////        )
    }
}
