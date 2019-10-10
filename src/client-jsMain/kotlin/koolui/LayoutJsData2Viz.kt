package koolui


import com.lightningkite.koolui.canvas.HtmlCanvas
import com.lightningkite.koolui.layout.Layout
import com.lightningkite.koolui.layout.views.LayoutViewWrapper
import com.lightningkite.koolui.layout.views.intrinsicLayout
import com.lightningkite.koolui.makeElement
import com.lightningkite.reacktive.property.ConstantObservableProperty
import com.lightningkite.reacktive.property.lifecycle.bind
import io.data2viz.viz.Viz
import io.data2viz.viz.bindRendererOn
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.EventListener
import kotlin.math.min

interface LayoutHtmlData2Viz : ViewFactoryData2Viz<Layout<*, HTMLElement>>, LayoutViewWrapper<HTMLElement> {
    override fun vizCanvas(draw: ConstantObservableProperty<Viz>, postSetup: (Viz) -> Unit): Layout<*, HTMLElement> {
        return intrinsicLayout(makeElement<HTMLCanvasElement>("canvas")) { layout ->
            val c = HtmlCanvas(this)
            layout.isAttached.bind(draw) { viz ->
                val canvas = c.element
                viz.bindRendererOn(canvas)
                canvas.addEventListener("resize",
                    EventListener { event ->
                        val size = min(canvas.height, canvas.width)
                        canvas.height = size
                        canvas.width = size
                        with(viz) {
                            height = canvas.height.toDouble()
                            width = canvas.width.toDouble()
                            resize(width, height)
                            render()
                        }
                    })
            }
        }
    }
}