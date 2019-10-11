package koolui


import com.lightningkite.koolui.canvas.HtmlCanvas
import com.lightningkite.koolui.layout.Layout
import com.lightningkite.koolui.layout.views.LayoutViewWrapper
import com.lightningkite.koolui.layout.views.intrinsicLayout
import com.lightningkite.koolui.lifecycle
import com.lightningkite.koolui.makeElement
import com.lightningkite.reacktive.property.ConstantObservableProperty
import com.lightningkite.reacktive.property.lifecycle.bind
import io.data2viz.viz.Viz
import io.data2viz.viz.bindRendererOn
import io.data2viz.viz.bindRendererOnNewCanvas
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.EventListener
import kotlin.browser.document
import kotlin.browser.window
import kotlin.math.min

interface LayoutHtmlData2Viz : ViewFactoryData2Viz<HTMLElement> {
    override fun vizCanvas(draw: ConstantObservableProperty<Viz>): HTMLElement {
        return makeElement<HTMLCanvasElement>("canvas").apply {
            val c = HtmlCanvas(this)
            lifecycle.bind(draw) { viz ->
                val canvas = c.element
                canvas.id = "vizCanvas"
                canvas.style.apply {
                    width = "100%"
                    height = "0%"
                    paddingBottom = "100%"
                }
//                viz.bindRendererOn(canvas.id)
                window.addEventListener("resize",
                    EventListener { event ->
                        println("canvas width: ${canvas.width} height: ${canvas.height}")
                        val size = min(canvas.clientWidth, canvas.clientHeight)
                        canvas.width = size
                        canvas.height = size
                        with(viz) {
                            height = canvas.height.toDouble()
                            width = canvas.width.toDouble()
                            resize(canvas.width.toDouble(), canvas.height.toDouble())
                            render()
                        }
                    })
//                postSetup(viz)
            }
        }
    }
}