import com.lightningkite.koolui.layout.Layout
import koolui.Factory
import com.lightningkite.koolui.views.root.contentRoot
import org.w3c.dom.HTMLElement
import penta.view.MainPentaVG
import kotlin.browser.document
import kotlin.browser.window

fun main(args: Array<String>) {
    val mainVg = MainPentaVG<Layout<*, HTMLElement>>()
    window.onload = {
        document.body!!.appendChild(
            Factory().run {
                    nativeViewAdapter(contentRoot(mainVg))
                }
//            Factory().contentRoot(mainVg)
        )
    }
}