package penta.view

import com.lightningkite.koolui.views.basic.text

class HistoryVG<VIEW>() : MyViewGenerator<VIEW> {
    override val title: String = "History"

    override fun generate(dependency: MyViewFactory<VIEW>): VIEW = with(dependency) {
        text("add boardState.history")
//        swap(
////            view = penta.client.PentaViz.gameState.history.onListUpdate.transform { list ->
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
