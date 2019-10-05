package penta.app

import io.data2viz.viz.JFxVizRenderer
import javafx.scene.layout.Priority
import PentaViz
import PentaViz.addEvents
import client
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import penta.ClientGameState
import penta.SerialNotation
import penta.json
import replayGame
import replaySetGrey
import tornadofx.*

class CanvasView : View("PentaGame") {
    val extraWidth = 1.0
    override val root = borderpane {
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS
//        this.minHeight = height
        val textarea = textarea("") {
            isEditable = false
        }
        val canvas = canvas(WIDTH, HEIGHT) {
            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS
            minHeight(HEIGHT)
            minWidth(WIDTH)
            val viz = PentaViz.viz

            val playerSymbols = listOf("triangle", "square", "cross", "circle")
            val playerCount = 3

            PentaViz.gameState.apply {
                updateLogPanel = { content ->
                    textarea.text = content
                }
            }
            with(viz) {
                width = this@canvas.width
                height = this@canvas.height
                resize(WIDTH, HEIGHT)
            }

            val renderer = JFxVizRenderer(this, viz)
            with(viz) {
                addEvents()
            }
            viz.renderer = renderer
            viz.render()

            widthProperty().onChange { newWidth ->
                println("new width: $newWidth")
                viz.resize(newWidth, height)
                viz.render()
            }
            heightProperty().onChange { newHeight ->
                println("new height: $newHeight")
//                viz.height = newHeight
                viz.resize(width, newHeight)
                viz.render()
            }

            if(false) {
                PentaViz.gameState.initialize(playerSymbols.subList(0, playerCount))
            } else {
                GlobalScope.launch {
                    client.ws(method = HttpMethod.Get, host = "127.0.0.1", port = 55555, path = "/replay") { // this: DefaultClientWebSocketSession
                        println("starting websocket connection")
                        outgoing.send(Frame.Text(replaySetGrey))

                        incoming.consumeEach {
                            val textFrame = it as? Frame.Text ?: return@consumeEach
                            val text = textFrame.readText()

                            println(text)

                            val notation = json.parse(SerialNotation.serializer(), text)

                            launch(Dispatchers.JavaFx) {
                                SerialNotation.toMoves(listOf(notation), PentaViz.gameState, false) { move ->
                                    PentaViz.gameState.processMove(move)
                                }
                            }
                        }
                        println("replay over")


    //            for (message in incoming.map { it as? Frame.Text }.filterNotNull()) {
    //                println(message.readText())
    //            }
                    }
                }
            }
        }
        this.center = canvas
        this.right = textarea

        widthProperty().onChange { newWidth ->
            if(newWidth > 250 + 250)
                canvas.width = newWidth * 0.5
        }
        heightProperty().onChange { newHeight ->
            if(newHeight > 250)
                canvas.height = newHeight
        }

    }
}