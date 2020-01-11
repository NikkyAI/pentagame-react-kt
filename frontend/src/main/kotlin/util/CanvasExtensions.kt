package util

import org.w3c.dom.CanvasRenderingContext2D
import kotlin.math.PI
import kotlin.math.min

fun CanvasRenderingContext2D.circle(x: Double, y: Double, radius: Double) {
    beginPath()
    arc(x, y, radius, 0.0, 2 * PI, false)

//    fill?.let {
//        context.fill()
//    }
//
//    stroke?.let {
//        context.stroke()
//    }

}
