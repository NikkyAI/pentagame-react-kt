package penta.app

import PentaViz
import com.lightningkite.koolui.color.ColorSet
import com.lightningkite.koolui.color.Theme
import com.lightningkite.koolui.layout.Layout
import com.lightningkite.koolui.layout.views.LayoutVFRootAndDialogs
import com.lightningkite.koolui.views.HasScale
import com.lightningkite.koolui.views.JavaFxLayoutWrapper
import com.lightningkite.koolui.views.Themed
import com.lightningkite.koolui.views.basic.LayoutJavaFxBasic
import com.lightningkite.koolui.views.graphics.LayoutJavaFxGraphics
import com.lightningkite.koolui.views.interactive.LayoutJavaFxInteractive
import com.lightningkite.koolui.views.layout.LayoutJavaFxLayout
import com.lightningkite.koolui.views.navigation.ViewFactoryNavigationDefault
import com.lightningkite.koolui.views.root.contentRoot
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.Scene
import javafx.stage.Stage
import koolui.LayoutJavaFxData2Viz
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import penta.PlayerState
import penta.view.MainPentaVG
import penta.view.MyViewFactory
import kotlin.system.exitProcess


class PentaApp : Application() {
    companion object {
        private val logger = KotlinLogging.logger {}
        @JvmStatic
        fun main(args: Array<String>) {
            launch(PentaApp::class.java)
        }
        val mainVg = MainPentaVG<Layout<*, Node>>()
    }

    class Factory(
        theme: Theme = penta.view.myTheme,
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
            LayoutJavaFxData2Viz /*ViewFactoryData2Viz*/ {
        override var root: Layout<*, Node>? = null
    }

    override fun start(primaryStage: Stage) {
        val root = with(Factory()) { nativeViewAdapter(contentRoot(mainVg)) }
        logger.info { "UI constructed" }

        val playerSymbols = listOf("triangle", "square", "cross", "circle")
        val playerCount = 3
        with(PentaViz) {
            viz.addEvents()
        }
        PentaViz.gameState.initialize(playerSymbols.subList(0, playerCount).map { PlayerState(it, it) })

        primaryStage.apply {
            scene = Scene(root, 1200.0, 600.0)
            show()
            setOnCloseRequest {
                Platform.exit()
                exitProcess(0)
            }
        }
        runBlocking {
            Dispatchers.Default
        }
    }
}