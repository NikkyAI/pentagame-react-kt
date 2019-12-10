package penta.view

import com.lightningkite.koolui.views.layout.vertical
import com.lightningkite.reacktive.property.ConstantObservableProperty
import showNotification

class ExperimentsVG<VIEW>() : MyViewGenerator<VIEW> {
    override val title: String = "Experiments"

    override fun generate(dependency: MyViewFactory<VIEW>): VIEW = with(dependency) {
        vertical {
            -button(
                label = ConstantObservableProperty("notification"),
                onClick = {
                    showNotification(
                        "Title",
                         "Body"
                    )
                }
            )
        }
    }
}
