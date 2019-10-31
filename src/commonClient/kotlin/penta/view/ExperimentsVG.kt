package penta.view

import com.lightningkite.kommon.string.Uri
import com.lightningkite.koolui.ExternalAccess
import com.lightningkite.koolui.async.suspendingTransform
import com.lightningkite.koolui.image.Image
import com.lightningkite.koolui.image.fromUrl
import com.lightningkite.koolui.image.withOptions
import com.lightningkite.koolui.views.basic.text
import com.lightningkite.koolui.views.layout.vertical
import com.lightningkite.lokalize.time.DateTime
import com.lightningkite.reacktive.property.ConstantObservableProperty
import com.lightningkite.reacktive.property.transform
import com.lightningkite.recktangle.Point
import penta.Constants
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
