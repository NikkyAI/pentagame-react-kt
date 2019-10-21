package penta.view

import com.lightningkite.kommon.string.Uri
import com.lightningkite.koolui.ExternalAccess
import com.lightningkite.koolui.async.suspendingTransform
import com.lightningkite.koolui.image.Image
import com.lightningkite.koolui.image.fromUrl
import com.lightningkite.koolui.image.withOptions
import com.lightningkite.koolui.views.basic.text
import com.lightningkite.koolui.views.layout.vertical
import com.lightningkite.lokalize.time.Date
import com.lightningkite.lokalize.time.DateTime
import com.lightningkite.reacktive.property.ConstantObservableProperty
import com.lightningkite.reacktive.property.transform
import com.lightningkite.recktangle.Point
import penta.Constants

class AboutVG<VIEW>() : MyViewGenerator<VIEW> {
    override val title: String = "About"

    val githubUrl = "https://github.com/NikkyAI/pentagame"
    val homepageUrl = "http://pentagame.org/"
    val ruleSheetUrl = "http://pentagame.org/pdf/Pentagame_Rulesheets.pdf"

    fun imagePropertyFromUrl(url: String, size: Point) = ConstantObservableProperty(url).suspendingTransform(
        default = Image.blank,
        transform = {
            Image.fromUrl(it) ?: Image.blank
        }
    ).transform { it.withOptions(size) }

    fun MyViewFactory<VIEW>.urlButton(url: String, imageUrl: String, size: Point)= imageButton(
        imageWithOptions = imagePropertyFromUrl(imageUrl, Point(48f, 48f)),
        label = ConstantObservableProperty(url),
        onClick = {
            ExternalAccess.openUri(Uri(url))
        }
    )

    override fun generate(dependency: MyViewFactory<VIEW>): VIEW = with(dependency) {
        vertical {
            val dateTime = DateTime.iso8601(Constants.RELEASE_TIME)
            -text("version: ${Constants.VERSION}")
            -text("commit: ${Constants.GIT_HASH}")
            -text("released: $dateTime")
            -urlButton(githubUrl, "https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png", Point(48f, 48f))
//            button(
//                label = ConstantObservableProperty(githubUrl),
//                onClick = {
//                    ExternalAccess.openUri(Uri(githubUrl))
//                }
//            )
            -button(
                label = ConstantObservableProperty(homepageUrl),
                onClick = {
                    ExternalAccess.openUri(Uri(homepageUrl))
                }
            )
            -button(
                label = ConstantObservableProperty(ruleSheetUrl),
                onClick = {
                    ExternalAccess.openUri(Uri(ruleSheetUrl))
                }
            )
            -text("TODO: write about the program")

        }
    }
}
