import PentaViz.addEvents
import io.data2viz.viz.bindRendererOn
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.features.websocket.WebSockets
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.EventListener
import penta.GameState
import kotlin.browser.document
import kotlin.browser.window
import kotlin.math.min

actual val client: HttpClient = HttpClient(Js).config {
    install(WebSockets)
}

fun main() {
    val canvasId = "viz"
    val canvas = requireNotNull(document.getElementById(canvasId) as HTMLCanvasElement?)
    { "No canvas in the document corresponding to $canvasId" }

    window.addEventListener("resize",
        EventListener { event ->
            val size = min(document.documentElement!!.clientHeight, document.documentElement!!.clientWidth)
            canvas.height = size
            canvas.width = size
            with(PentaViz.viz) {
                height = canvas.height.toDouble()
                width = canvas.width.toDouble()
                resize(width, height)
                render()
            }
        }
    )

    val size = min(document.documentElement!!.clientHeight, document.documentElement!!.clientWidth)
    canvas.height = size
    canvas.width = size
    with(PentaViz.viz) {
        height = canvas.height.toDouble()
        width = canvas.width.toDouble()

        PentaViz.gameState = GameState(
            listOf("a", "b", "c"),
            mapOf()
        )

        resize(width, height)
        bindRendererOn(canvas)
        addEvents()
        render()
    }


}