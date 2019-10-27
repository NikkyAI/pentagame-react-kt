package penta.view

import com.lightningkite.koolui.concepts.TextSize
import com.lightningkite.koolui.geometry.AlignPair
import com.lightningkite.koolui.views.basic.text
import com.lightningkite.koolui.views.navigation.pagesEmbedded
import com.lightningkite.reacktive.property.StandardObservableProperty

class RulesVG<VIEW>() : MyViewGenerator<VIEW> {
    override val title: String = "Rules"

    override fun generate(dependency: MyViewFactory<VIEW>): VIEW = with(dependency) {
        pagesEmbedded(
            dependency,
            StandardObservableProperty(0),
//            {
//                // TODO: get website with just rules
////                web(ConstantObservableProperty("http://pentagame.org/"))
//            },
            {
                text(text = "Rules page 1", size = TextSize.Header, align = AlignPair.CenterCenter)
            },
            {
                text(text = "Rules page 2", size = TextSize.Header, align = AlignPair.CenterCenter)
            },
            {
                text(text = "Rules page 3", size = TextSize.Header, align = AlignPair.CenterCenter)
            }
        )
    }
}
