package penta.app

import io.data2viz.viz.JFxVizRenderer
import javafx.scene.layout.Priority
import PentaViz
import PentaViz.addEvents
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import kotlinx.serialization.modules.SerializersModule
import penta.ClientGameState
import penta.SerialNotation
import tornadofx.*
import java.io.File

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

            val playerSymbols = listOf("square", "triangle", "cross", "circle")
            val playerCount = 2

            PentaViz.gameState = ClientGameState(
                playerSymbols.subList(0, playerCount)
            ) { content ->
                textarea.text = content
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

            PentaViz.gameState.initialize(playerSymbols.subList(0, playerCount))


//            val json = Json(JsonConfiguration(unquoted = false, allowStructuredMapKeys = true, prettyPrint = true, classDiscriminator = "type"), context = SerializersModule {
//                SerialNotation.install(this)
//            })
//
//            val testFile = File(System.getProperty("user.home")).resolve("dev/pentagame/src/client-fxTest/resources/test2.json")
//            val testJson = testFile.readText()
//            val notationList = json.parse(SerialNotation.serializer().list, testJson)
//
//            notationList.forEach {
//                println(it)
//            }
//            val moves = SerialNotation.toMoves(notationList, PentaViz.gameState) {
//                PentaViz.gameState.processMove(it)
//                PentaViz.updateBoard()
//                Thread.sleep(1000)
//            }
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