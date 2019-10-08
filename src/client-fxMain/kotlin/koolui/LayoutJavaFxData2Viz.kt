package koolui

import com.lightningkite.koolui.layout.Layout
import com.lightningkite.koolui.layout.views.LayoutViewWrapper
import com.lightningkite.koolui.layout.views.wrap
import com.lightningkite.koolui.views.HasScale
import com.lightningkite.reacktive.property.ConstantObservableProperty
import com.lightningkite.reacktive.property.lifecycle.bind
import io.data2viz.viz.JFxVizRenderer
import io.data2viz.viz.Viz
import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

interface LayoutJavaFxData2Viz : ViewFactoryData2Viz<Layout<*, Node>>, /*HasScale,*/ LayoutViewWrapper<Node> {

    class SquareCanvas: Canvas(300.0 ,300.0) {
        var onResize: ()->Unit = {}
        override fun isResizable(): Boolean = true
        override fun resize(width: Double, height: Double) {
            val side = if (height > width) width else height
            super.setWidth(side)
            super.setHeight(side)
            onResize()
        }
    }
    override fun vizCanvas(viz: ConstantObservableProperty<Viz>, postSetup: (Viz)->Unit): Layout<*, Node> {
        return wrap(SquareCanvas()) { lifecycle ->
            // TODO: resize only in square ?
            lifecycle.bind(viz){ viz ->
                println("initalizing viz")
                JFxVizRenderer(
                    canvas = this,
                    viz = viz
                )
                onResize = {
                    viz.resize(width, height)
                    viz.render()
                }
                postSetup(viz)
                viz.render()
            }
        }
    }
}