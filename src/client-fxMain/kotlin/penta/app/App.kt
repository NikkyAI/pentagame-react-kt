package penta.app

import PentaViz
import com.lightningkite.koolui.ApplicationAccess
import com.lightningkite.koolui.color.ColorSet
import com.lightningkite.koolui.color.Theme
import com.lightningkite.koolui.layout.Layout
import com.lightningkite.koolui.layout.views.LayoutVFRootAndDialogs
import com.lightningkite.koolui.views.*
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
import penta.view.myTheme

class App : Application() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(App::class.java)
        }

        val mainVg = MainPentaVG<Layout<*, Node>>()
    }

    //Here, you pick out what GUI modules you want to use
    class Factory(
        theme: Theme = myTheme,
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
        val viz = PentaViz.viz

        val playerSymbols = listOf("triangle", "square", "cross", "circle")
        val playerCount = 3

        // TODO: register callbacks
        PentaViz.gameState.apply {
            updateLogPanel = { content ->
                // TODO: textarea.text = content
            }
        }
        ApplicationAccess.init(App::class.java.classLoader, primaryStage)
        val root = with(Factory()) { nativeViewAdapter(contentRoot(mainVg)) }
        primaryStage.scene = Scene(root)
        primaryStage.show()
    }
}