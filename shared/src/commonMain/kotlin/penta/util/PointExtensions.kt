package penta.util

import io.data2viz.geom.Point
import io.data2viz.math.deg
import kotlin.math.sqrt

val Point.length
    get() = sqrt((x * x) + (y * y))

val Point.unit: Point
    get() = this / length

fun onCircle(r: Double, angle: Double): Point = Point(
    angle.deg.cos * r,
    angle.deg.sin * r
)

fun Point.interpolate(otherPoint: Point, steps: Int = 1, skip: Double = 0.0): List<Point> {
    val v = Point(
        (otherPoint.x - x) / (steps + 1),
        (otherPoint.y - y) / (steps + 1)
    ).unit

    return (0 until steps).map { i ->
        this + (v * i) + v * skip
    }
}

