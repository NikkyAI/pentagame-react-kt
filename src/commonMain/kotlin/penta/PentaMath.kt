import io.data2viz.geom.Point
import kotlin.math.sqrt

object PentaMath {
    val PHI = (sqrt(5.0) + 1.0) / 2.0
    // distances
    val k = 3
    val l = 6
    // diameter of unit
    val s = 1.0
    // diameter
    val c = sqrt(5.0) // (2.0/PHI) + 1.0
    // diameters
    val j = (9.0 - (2.0 * sqrt(5.0))) / sqrt(5.0) // (c+9.0+sqrt(5.0))/sqrt(5.0)
    val L = c + 12 + j
    val K = (2.0 * j) + 6
    val d = 2.0 * (c + 12 + j) + (2 * j + 6) // (2.0*L) + K

    // diameter to outer ring
    val r = (2.0 / 5.0) * sqrt(1570 + (698.0 * sqrt(5.0)))// d / sqrt(PHI + 2.0)
    // diameter with
    val R_ = r + c
    val inner_r = ((k + j) * 2 * (1.0 + sqrt(5.0))) / sqrt(2.0 * (5.0 + sqrt(5.0)))

    fun fiveRoots(p: Point): Point {
        if (p.y == 0.0) return p
        return Point(
            ((p.x * sqrt(5.0) - 1.0) / 4.0), p.y
                    * sqrt((5.0 + (p.x * sqrt(5.0))) / 8.0)
        )
    }
}

//fun main() {
//    with(PentaMath) {
//        println("s: $s")
//        println("c: $c")
//        println("j: $j")
//        println("r: $r")
//        println("R: $R_")
//        println("inner_r: $inner_r")
//
//        // counterclockwise
//        println(" 0,  0: {${fiveRoots(penta.math.Point(1.0, 0.0)) * r},") // A
//        println("+1, +1: {${fiveRoots(penta.math.Point(1.0, +1.0)) * r} },") // E
//        println("+1, -1: {${fiveRoots(penta.math.Point(+1.0, -1.0)) * r} },") // D
//        println("-1, -1: {${fiveRoots(penta.math.Point(-1.0, -1.0)) * r} },") // C
//        println("-1, +1: {${fiveRoots(penta.math.Point(-1.0, +1.0)) * r} },") // B
//        println(" 0,  0: {${fiveRoots(penta.math.Point(1.0, 0.0))*-inner_r}},") // a
//        println("+1, +1: {${fiveRoots(penta.math.Point(+1.0, +1.0)) * -inner_r}},") // e
//        println("+1, -1: {${fiveRoots(penta.math.Point(+1.0, -1.0)) * -inner_r}},") // d
//        println("-1, -1: {${fiveRoots(penta.math.Point(-1.0, -1.0)) * -inner_r}},") // c
//        println("-1, +1: {${fiveRoots(penta.math.Point(-1.0, +1.0)) * -inner_r}}") // b
//    }
//
////    val center = penta.math.Point(50, 50)
////    (0..5).forEach { i ->
////        val point = center.onCircle(72.0*i, PentaMathUnit.R)
////        println("$i: $point")
////    }
////    (0..5).forEach { i ->
////        val point = center.onCircle(72.0*i + 36.0, PentaMathUnit.p)
////        println("$i: $point")
////    }
////
////    val a =penta.math.Point(0, 0)
////    val b = penta.math.Point(100, 100)
////    (1..3).forEach { steps ->
////        val points = a.interpolate(b, steps)
////        println("steps: $steps, points: $points")
////    }
//}