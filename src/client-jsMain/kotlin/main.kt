import PentaViz.addEvents
import io.data2viz.viz.bindRendererOn
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.EventListener
import org.w3c.dom.url.URL
import penta.ClientGameState
import penta.SerialNotation
import penta.json
import kotlin.browser.document
import kotlin.browser.window
import kotlin.math.min

actual val client: HttpClient = HttpClient(Js).config {
    install(WebSockets)
}

suspend fun main(): Unit = coroutineScope {
    val canvasId = "viz"
    val canvas = requireNotNull(document.getElementById(canvasId) as HTMLCanvasElement?)
    { "No canvas in the document corresponding to $canvasId" }

    val url = URL(document.URL)
    val hash = url.hash.ifEmpty { null }?.substringAfter('#')
    println("hash: $hash")
    val playerCount = hash?.toInt() ?: 2
    require(playerCount in (2..4)) { "player number must be within 2..4" }

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

    val playerSymbols = listOf("triangle", "square", "cross", "circle")

    with(PentaViz.viz) {
        height = canvas.height.toDouble()
        width = canvas.width.toDouble()

//        PentaViz.gameState = ClientGameState(
//            playerSymbols.subList(0, playerCount)
//        )

        resize(width, height)
        bindRendererOn(canvas)
        addEvents()
        render()

//        PentaViz.gameState.initialize(playerSymbols.subList(0, playerCount))
    }

//    val wsConnection = launch {
    if (true) {
        PentaViz.gameState.initialize(playerSymbols.subList(0, playerCount))
    } else {
        client.ws(method = HttpMethod.Get, host = "127.0.0.1", port = 55555, path = "/replay") {
            // this: DefaultClientWebSocketSession
            println("starting websocket connection")
            outgoing.send(Frame.Text(replayGame))

            incoming.consumeEach {
                val textFrame = it as? Frame.Text ?: return@consumeEach
                val text = textFrame.readText()

                println(text)

                val notation = json.parse(SerialNotation.serializer(), text)

                SerialNotation.toMoves(listOf(notation), PentaViz.gameState, false) { move ->
                    PentaViz.gameState.processMove(move)
                }
            }
            println("replay over")
        }
//        }
    }

}