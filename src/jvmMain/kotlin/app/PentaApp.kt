package app

import javafx.application.Application
import tornadofx.*

val SCALE = 30
val WIDTH = PentaMath.R_ * SCALE
val HEIGHT = PentaMath.R_ * SCALE

fun main(args: Array<String>) {
    Application.launch(PentaApp::class.java, *args)
}

class PentaApp : App() {
    override val primaryView = CanvasView::class
}