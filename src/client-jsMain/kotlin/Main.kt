
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
import koolui.HtmlViewFactoryOverrides
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLDivElement
import penta.view.myTheme
import kotlin.math.min

//class LayoutFactory(
//    val underlying: LayoutHtmlViewFactory = LayoutHtmlViewFactory(penta.view.theme)
//) : MyViewFactory<Layout<*, HTMLElement>>, ViewFactory<Layout<*, HTMLElement>> by underlying, LayoutHtmlData2Viz by underlying


class Factory :
    MyViewFactory<HTMLElement>,
    ViewFactory<HTMLElement> by HtmlViewFactoryOverrides(myTheme),
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
        val container = requireNotNull(document.getElementById("vizContainer") as HTMLDivElement?)
        with(PentaViz) {
            viz.bindRendererOn(canvas)
            viz.addEvents()
            gameState.initialize(playerSymbols.subList(0, playerCount))
        }
        val rect = container.getBoundingClientRect()
        val size =  min(
            min(rect.width, rect.height).toInt(),
            window.document.documentElement!!.clientHeight
        )
//
//           ,
//
//        )
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