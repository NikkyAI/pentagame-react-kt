package penta.app

import PentaViz
import PentaViz.addEvents
import client
import com.lightningkite.koolui.color.ColorSet
import com.lightningkite.koolui.color.Theme
import com.lightningkite.koolui.layout.Layout
import com.lightningkite.koolui.layout.views.LayoutVFRootAndDialogs
import com.lightningkite.koolui.layout.views.LayoutViewWrapper
import com.lightningkite.koolui.views.HasScale
import com.lightningkite.koolui.views.JavaFxLayoutWrapper
import com.lightningkite.koolui.views.LayoutJavaFxViewFactory
import com.lightningkite.koolui.views.Themed
import com.lightningkite.koolui.views.ViewFactory
import com.lightningkite.koolui.views.basic.LayoutJavaFxBasic
import com.lightningkite.koolui.views.graphics.LayoutJavaFxGraphics
import com.lightningkite.koolui.views.interactive.LayoutJavaFxInteractive
import com.lightningkite.koolui.views.layout.LayoutJavaFxLayout
import com.lightningkite.koolui.views.navigation.ViewFactoryNavigationDefault
import com.lightningkite.koolui.views.root.contentRoot
import javafx.application.Application
import javafx.scene.Node
import javafx.scene.Scene
import javafx.stage.Stage
import koolui.LayoutJavaFxData2Viz
import penta.view.MainPentaVG
import penta.view.MyViewFactory

class App : Application() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(App::class.java)
        }

        val mainVg = MainPentaVG<Layout<*, Node>>()
    }

//    class MyFactory(val basedOn: LayoutJavaFxViewFactory = LayoutJavaFxViewFactory(penta.view.theme)) :
//        MyViewFactory<Layout<*, Node>>, ViewFactory<Layout<*, Node>> by basedOn

    class Factory(
        theme: Theme = penta.view.theme,
        colorSet: ColorSet = theme.main,
        override val scale: Double = 1.0
    ) : MyViewFactory<Layout<*, Node>>,
            HasScale,
            Themed by Themed.impl(theme, colorSet),
            LayoutJavaFxBasic /*ViewFactoryBasic*/,
            LayoutJavaFxInteractive /*ViewFactoryInteractive*/,
            LayoutJavaFxGraphics /*ViewFactoryGraphics*/,
            LayoutJavaFxLayout /*ViewFactoryLayout*/,
            ViewFactoryNavigationDefault<Layout<*, Node>> /*ViewFactoryNavigation*/,
            LayoutVFRootAndDialogs<Node> /*ViewFactoryDialogs*/,
            JavaFxLayoutWrapper /*ViewLayoutWrapper*/,
            LayoutJavaFxData2Viz {
        override var root: Layout<*, Node>? = null
    }

    override fun start(stage: Stage) {
        val root = with(Factory()) { nativeViewAdapter(contentRoot(mainVg)) }

        val playerSymbols = listOf("triangle", "square", "cross", "circle")
        val playerCount = 3
        with(PentaViz) {
            viz.addEvents()
        }
        PentaViz.gameState.initialize(playerSymbols.subList(0, playerCount))

        stage.let {
            it.scene = Scene(root)
            it.show()
        }
//        JFxVizRenderer(canvas, viz)
//        with(viz) {
//            resize(canvas.width, canvas.height)
//            addEvents()
//            render()
//        }

//        if (true) {
//            PentaViz.gameState.initialize(playerSymbols.subList(0, playerCount))
//        } else {
//            GlobalScope.launch {
//                client.ws(method = HttpMethod.Get, host = "127.0.0.1", port = 55555, path = "/replay") {
//                    // this: DefaultClientWebSocketSession
//                    println("starting websocket connection")
//                    outgoing.send(Frame.Text(replaySetGrey))
//
//                    incoming.consumeEach {
//                        val textFrame = it as? Frame.Text ?: return@consumeEach
//                        val text = textFrame.readText()
//
//                        println(text)
//
//                        val notation = json.parse(SerialNotation.serializer(), text)
//
//                        launch(Dispatchers.JavaFx) {
//                            SerialNotation.toMoves(listOf(notation), PentaViz.gameState, false) { move ->
//                                PentaViz.gameState.processMove(move)
//                            }
//                        }
//                    }
//                    println("replay over")
//                }
//            }
//        }
    }
}