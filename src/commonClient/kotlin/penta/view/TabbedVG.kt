package penta.view

import com.lightningkite.kommon.collection.push
import com.lightningkite.koolui.color.Color
import com.lightningkite.koolui.concepts.Importance
import com.lightningkite.koolui.image.MaterialIcon
import com.lightningkite.koolui.image.color
import com.lightningkite.koolui.image.withOptions
import com.lightningkite.reacktive.list.MutableObservableList
import com.lightningkite.reacktive.list.observableListOf
import com.lightningkite.reacktive.property.ConstantObservableProperty
import com.lightningkite.reacktive.property.transform
import com.lightningkite.recktangle.Point

class TabbedVG<VIEW>(
    val stack: MutableObservableList<MyViewGenerator<VIEW>>
) : MyViewGenerator<VIEW> {
    override val title: String = "KotlinX UI Test"

    val tests = observableListOf<Pair<String, () -> MyViewGenerator<VIEW>>>(
//            "Space Test" to { SpaceTestVG<VIEW>() },
//            "Original Test" to { OriginalTestVG<VIEW>() },
//            "Alpha" to { AlphaTestVG<VIEW>() },
//            "Horizontal" to { HorizontalVG<VIEW>() },
//            "Vertical" to { VerticalTestVG<VIEW>() },
//            "Pages" to { PagesVG<VIEW>() },
//            "Frame" to { FrameVG<VIEW>() },
////            "Http Call Test" to { HttpCallTestVG<VIEW>() },
//            "Controls" to { ControlsVG<VIEW>() },
//            "Notifications" to { NotificationTestVG<VIEW>() },
//            "Icons" to { IconsTestVG<VIEW>() },
////            "URL Image Test" to { UrlImageTestVG<VIEW>() },
//            "Dialog" to { DialogTestVG<VIEW>() },
        "Rules" to { RulesVG<VIEW>() }
    )

    override fun generate(dependency: MyViewFactory<VIEW>): VIEW = with(dependency) {
        list(data = tests, makeView = { itemObs, index ->
            button(
                imageWithOptions = ConstantObservableProperty(
                    MaterialIcon.adb.color(Color.black).withOptions(
                        Point(
                            24f,
                            24f
                        )
                    )
                ),
                label = itemObs.transform { item -> item.first },
                importance = Importance.Low,
                onClick = {
                    stack.push(itemObs.value.second.invoke())
                }
            )
        }).margin(8f)
    }
}
