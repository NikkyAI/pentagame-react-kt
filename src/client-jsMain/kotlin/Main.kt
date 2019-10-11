
import com.lightningkite.koolui.views.HtmlViewFactory
import com.lightningkite.koolui.views.ViewFactory
import koolui.LayoutHtmlData2Viz
import org.w3c.dom.HTMLElement
import penta.view.MainPentaVG
import penta.view.MyViewFactory
import kotlin.browser.document
import kotlin.browser.window
import com.lightningkite.koolui.views.root.contentRoot
import io.data2viz.viz.bindRendererOn
import org.w3c.dom.HTMLCanvasElement
import kotlin.math.min

//class LayoutFactory(
//    val underlying: LayoutHtmlViewFactory = LayoutHtmlViewFactory(penta.view.theme)
//) : MyViewFactory<Layout<*, HTMLElement>>, ViewFactory<Layout<*, HTMLElement>> by underlying, LayoutHtmlData2Viz by underlying


class Factory() :
    MyViewFactory<HTMLElement>,
    ViewFactory<HTMLElement> by HtmlViewFactory(penta.view.theme),
    LayoutHtmlData2Viz


fun main(args: Array<String>) {
    val mainVg = MainPentaVG<HTMLElement>()
    window.onload = {
        document.body!!.appendChild(
//            Factory().run {
//                    nativeViewAdapter(contentRoot(mainVg))
//                }
            Factory().contentRoot(mainVg)
        )
        println("UI finished")

        val playerSymbols = listOf("triangle", "square", "cross", "circle")
        val playerCount = 3
        val canvasId = "vizCanvas"
        val canvas = requireNotNull(document.getElementById(canvasId) as HTMLCanvasElement?)
        with(PentaViz) {
            viz.bindRendererOn(canvas)
            viz.addEvents()
            gameState.initialize(playerSymbols.subList(0, playerCount))
        }
        val size = min(canvas.clientWidth, canvas.clientHeight)
        println("initial size: $size")
        canvas.width = size
        canvas.height = size
        with(PentaViz.viz) {
            height = canvas.height.toDouble()
            width = canvas.width.toDouble()
            resize(canvas.width.toDouble(), canvas.height.toDouble())
            render()
        }
    }
}