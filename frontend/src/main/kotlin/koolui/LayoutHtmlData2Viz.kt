package koolui

import com.lightningkite.koolui.appendLifecycled
import com.lightningkite.koolui.lifecycle
import com.lightningkite.koolui.makeElement
import com.lightningkite.reacktive.property.ObservableProperty
import com.lightningkite.reacktive.property.lifecycle.bind
import io.data2viz.viz.Viz
import mu.KotlinLogging
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.EventListener
import kotlin.browser.window
import kotlin.math.min

interface LayoutHtmlData2Viz : ViewFactoryData2Viz<HTMLElement> {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun vizCanvas(draw: ObservableProperty<Viz>): HTMLElement {
        return makeElement<HTMLDivElement>("div").apply {
            lifecycle.bind(draw) { viz ->
                id = "vizContainer"
                val canvas = makeElement<HTMLCanvasElement>("canvas")
                canvas.id = "vizCanvas"
                canvas.style.apply {
                    margin = "0px"
                }
//                viz.bindRendererOn(canvas.id)
                window.addEventListener("resize",
                    EventListener { event ->
                        val rect = getBoundingClientRect()
                        logger.debug { "rect width: ${rect.width} height: ${rect.height}" }
                        logger.debug { "canvas width: ${canvas.width} height: ${canvas.height}" }
                        val size = min(
                            min(rect.height, rect.width).toInt(),
                            window.document.documentElement!!.clientHeight
                        )
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
                appendLifecycled(canvas)
            }
        }
    }
}