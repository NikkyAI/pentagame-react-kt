package penta.view

import com.lightningkite.koolui.color.Theme
import com.lightningkite.koolui.views.ViewFactory
import com.lightningkite.koolui.views.ViewGenerator
import koolui.ViewFactoryData2Viz

val myTheme = Theme.light()
interface MyViewFactory<VIEW> :
    ViewFactory<VIEW>,
    ViewFactoryData2Viz<VIEW>
typealias MyViewGenerator<VIEW> = ViewGenerator<MyViewFactory<VIEW>, VIEW>
