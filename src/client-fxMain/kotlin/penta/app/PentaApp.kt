package penta.app

import javafx.application.Application

val SCALE = 30
val WIDTH = PentaMath.R_ * SCALE
val HEIGHT = PentaMath.R_ * SCALE

fun main(args: Array<String>) {
    Application.launch(PentaApp::class.java, *args)
}

@Deprecated("use App")
class PentaApp : tornadofx.App() {
    override val primaryView = PentaView::class
}