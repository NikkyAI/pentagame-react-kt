package penta.util


//fun io.data2viz.color.Color.asKoolUIColor() =
//    com.lightningkite.koolui.color.Color(
//        alpha.value.toFloat(),
//        r.toFloat() / 256,
//        g.toFloat() / 256,
//        b.toFloat() / 256
//    )
//fun MutableObservableProperty<io.data2viz.color.Color>.asKoolUIColor() = transform{
//    it.run {
//        com.lightningkite.koolui.color.Color(
//            alpha.value.toFloat(),
//            r.toFloat() / 256,
//            g.toFloat() / 256,
//            b.toFloat() / 256
//        )
//    }
//
//}


//fun Image.Companion.fromSvgString(data: String, color: Color): Image {
//    val webColor = color.toAlphalessWeb()
//    return Image.fromSvgString(
////        data.substringBefore('g') + """g fill="${color.toAlphalessWeb()}"""" + data.substringAfter('g')
//        data.replace("<path ", "<path fill=\"$webColor\" ")
//            .replace("<circle ", "<circle fill=\"$webColor\" ")
//    )
//}