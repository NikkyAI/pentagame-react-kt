//package penta.util
//
//import io.data2viz.color.Color
//import io.data2viz.scale.ScalesChromatic
//import mu.KotlinLogging
//
//private val logger = Logger(this::class.simpleName!!)
//fun Color.interpolate(other: Color, steps: Int): List<Color> {
//    // TODO: use custom chromatic scales
//    val scale = ScalesChromatic.Continuous.linearHCL{
//        domain = listOf(0.0, steps+1.0)
//        range = listOf(this@interpolate, other)
//    }
//
//    val ticks = scale.ticks(steps+1).drop(1).dropLast(1)
//    logger.trace { "steps: $steps, ticks: $ticks, ${ticks.size}" }
//    return ticks.map { d ->
//        scale(d)
//    }
//}
