import com.lightningkite.koolui.views.ViewFactory
import com.lightningkite.koolui.views.root.contentRoot
import io.data2viz.viz.bindRendererOn
import koolui.HtmlViewFactoryOverrides
import koolui.LayoutHtmlData2Viz
import mu.KotlinLogging
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import penta.PlayerState
import penta.view.MainPentaVG
import penta.view.MyViewFactory
import penta.view.myTheme
import kotlin.browser.document
import kotlin.browser.window
import kotlin.math.min

//class LayoutFactory(
//    val underlying: LayoutHtmlViewFactory = LayoutHtmlViewFactory(penta.view.theme)
//) : MyViewFactory<Layout<*, HTMLElement>>, ViewFactory<Layout<*, HTMLElement>> by underlying, LayoutHtmlData2Viz by underlying

private val logger = KotlinLogging.logger {}

class Factory :
    MyViewFactory<HTMLElement>,
    ViewFactory<HTMLElement> by HtmlViewFactoryOverrides(myTheme),
    LayoutHtmlData2Viz

//fun main(args: Array<String>) {
//    logger.info { "running main" }
//    val mainVg = MainPentaVG<HTMLElement>()
//    document.body!!.appendChild(
//        Factory().contentRoot(mainVg)
//    )
//    logger.info { "UI finished" }
//
//    val playerSymbols = listOf("square", "cross", "circle")
//    val playerCount = 2
//    val canvasId = "vizCanvas"
//    val canvas = requireNotNull(document.getElementById(canvasId) as HTMLCanvasElement?)
//    val container = requireNotNull(document.getElementById("vizContainer") as HTMLDivElement?)
//    with(PentaViz) {
//        viz.bindRendererOn(canvas)
//        viz.addEvents()
//        gameState.initialize(playerSymbols.subList(0, playerCount).map { PlayerState(it, it) })
//    }
//    val rect = container.getBoundingClientRect()
//    val size = min(
//        min(rect.width, rect.height).toInt(),
//        window.document.documentElement!!.clientHeight
//    )
//    logger.info { "initial size: $size" }
//    canvas.width = size
//    canvas.height = size
//    with(PentaViz.viz) {
//        height = canvas.height.toDouble()
//        width = canvas.width.toDouble()
//        resize(canvas.width.toDouble(), canvas.height.toDouble())
//        render()
//    }
//}