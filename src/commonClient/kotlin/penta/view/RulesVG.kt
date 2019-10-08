package penta.view

import com.lightningkite.reacktive.property.ConstantObservableProperty
import com.lightningkite.reacktive.property.StandardObservableProperty
import com.lightningkite.koolui.concepts.TextSize
import com.lightningkite.koolui.geometry.AlignPair
import com.lightningkite.koolui.views.basic.*
import com.lightningkite.koolui.image.MaterialIcon
import com.lightningkite.koolui.views.navigation.pagesEmbedded
import penta.view.MyViewFactory
import penta.view.MyViewGenerator

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
                text(text = "Second page", size = TextSize.Header, align = AlignPair.CenterCenter)
            },
            {
                text(text = "Third page", size = TextSize.Header, align = AlignPair.CenterCenter)
            },
            {
                text(text = "Last page", size = TextSize.Header, align = AlignPair.CenterCenter)
            }
        )
    }
}
