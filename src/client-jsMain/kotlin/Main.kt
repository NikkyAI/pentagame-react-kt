
import com.lightningkite.koolui.views.HtmlViewFactory
import com.lightningkite.koolui.views.ViewFactory
import koolui.LayoutHtmlData2Viz
import org.w3c.dom.HTMLElement
import penta.view.MainPentaVG
import penta.view.MyViewFactory
import kotlin.browser.document
import kotlin.browser.window
import com.lightningkite.koolui.views.root.contentRoot

//class LayoutFactory(
//    val underlying: LayoutHtmlViewFactory = LayoutHtmlViewFactory(penta.view.theme)
//) : MyViewFactory<Layout<*, HTMLElement>>, ViewFactory<Layout<*, HTMLElement>> by underlying, LayoutHtmlData2Viz by underlying


class Factory() :
    MyViewFactory<HTMLElement>,
    ViewFactory<HTMLElement> by HtmlViewFactory(penta.view.theme),
    LayoutHtmlData2Viz


fun main(args: Array<String>) {
    val mainVg = MainPentaVG<HTMLElement>()
    window.onload = {
        document.body!!.appendChild(
//            Factory().run {
//                    nativeViewAdapter(contentRoot(mainVg))
//                }
            Factory().contentRoot(mainVg)
        )
    }
}