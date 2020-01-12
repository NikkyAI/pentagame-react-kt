import io.data2viz.geom.Point
import kotlin.math.sqrt

object PentaMath {
    val PHI = (sqrt(5.0) + 1.0) / 2.0
    // distances
    const val k = 3
    const val l = 6
    // diameter of unit
    const val s = 1.0
    // diameter
    val c = sqrt(5.0) // (2.0/PHI) + 1.0
    // diameters
    val j = (9.0 - (2.0 * sqrt(5.0))) / sqrt(5.0) // (c+9.0+sqrt(5.0))/sqrt(5.0)
    val L = c + 12 + j
    val K = (2.0 * j) + 6
    val d = 2.0 * (c + 12 + j) + (2 * j + 6) // (2.0*L) + K

    // diameter of outer ring
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