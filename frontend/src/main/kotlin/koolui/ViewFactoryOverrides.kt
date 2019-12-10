package koolui

import com.lightningkite.koolui.appendLifecycled
import com.lightningkite.koolui.async.UI
import com.lightningkite.koolui.color.ColorSet
import com.lightningkite.koolui.color.Theme
import com.lightningkite.koolui.concepts.Animation
import com.lightningkite.koolui.concepts.TextSize
import com.lightningkite.koolui.geometry.Align
import com.lightningkite.koolui.geometry.AlignPair
import com.lightningkite.koolui.geometry.LinearPlacement
import com.lightningkite.koolui.image.MaterialIcon
import com.lightningkite.koolui.image.color
import com.lightningkite.koolui.image.withOptions
import com.lightningkite.koolui.removeLifecycled
import com.lightningkite.koolui.toWeb
import com.lightningkite.koolui.views.ViewGenerator
import com.lightningkite.koolui.views.basic.work
import com.lightningkite.koolui.views.interactive.imageButton
import com.lightningkite.koolui.views.layout.horizontal
import com.lightningkite.koolui.views.layout.space
import com.lightningkite.koolui.views.layout.vertical
import com.lightningkite.reacktive.property.ConstantObservableProperty
import com.lightningkite.reacktive.property.MutableObservableProperty
import com.lightningkite.reacktive.property.ObservableProperty
import com.lightningkite.reacktive.property.lifecycle.bind
import com.lightningkite.reacktive.property.transform
import com.lightningkite.recktangle.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

open class HtmlViewFactoryOverrides(
    theme: Theme,
    colorSet: ColorSet = theme.main
) : OpenHtmlViewFactory(theme, colorSet) {
    override fun <DEPENDENCY> pages(
        dependency: DEPENDENCY,
        page: MutableObservableProperty<Int>,
        vararg pageGenerator: ViewGenerator<DEPENDENCY, HTMLElement>
    ): HTMLElement = vertical {
        var previous = page.value
        +swap(page.transform {
            val anim = when {
                page.value < previous -> Animation.Pop
                page.value > previous -> Animation.Push
                else -> Animation.Fade
            }
            previous = it
            pageGenerator[it.coerceIn(pageGenerator.indices)].generate(dependency) to anim
        })
        -horizontal {
            -imageButton(
                imageWithOptions = ConstantObservableProperty(
                    com.lightningkite.koolui.image.MaterialIcon.chevronLeft.color(colorSet.foreground).withOptions(
                        defaultSize = Point(24f, 24f)
                    )
                ),
                onClick = {
                    page.value = page.value.minus(1).coerceIn(pageGenerator.indices)
                }
            )
            +space()
            -text(
                text = page.transform { "${it + 1} / ${pageGenerator.size}" },
                size = TextSize.Tiny
            )
            +space()
            -imageButton(
                imageWithOptions = ConstantObservableProperty(
                    com.lightningkite.koolui.image.MaterialIcon.chevronRight.color(colorSet.foreground).withOptions(
                        defaultSize = Point(24f, 24f)
                    )
                ),
                onClick = {
                    page.value = page.value.plus(1).coerceIn(pageGenerator.indices)
                }
            )
        }
    }

    override fun refresh(
        contains: HTMLElement,
        working: ObservableProperty<Boolean>,
        onRefresh: () -> Unit
    ): HTMLElement = vertical {
        horizontal {
            -text(ConstantObservableProperty("custom"))
            +space()
            -work(
                imageButton(
                    imageWithOptions = MaterialIcon.refresh.color(theme.main.foreground).withOptions(
                        Point(
                            24f,
                            24f
                        )
                    )
                ) {
                    onRefresh.invoke()
                },
                working
            )
        }
        +contains
    }

    override fun align(vararg views: Pair<AlignPair, HTMLElement>): HTMLElement = makeElement<HTMLDivElement>("div") {
        style.maxWidth = "100%"
        style.maxHeight = "100%"
        style.position = "relative"
        val reuse = Point()
        var bestX = 0f
        var bestY = 0f
        for (view in views) {
            measure(view.second, reuse)
            if (reuse.x > bestX) bestX = reuse.x
            if (reuse.y > bestY) bestY = reuse.y
        }
//        style.minWidth = bestX.toString() + "px"
//        style.minHeight = bestY.toString() + "px"
        for ((align, view) in views) {
            view.style.position = "absolute"
            when (align.horizontal) {
                Align.Start -> view.style.left = "0px"
                Align.Center -> {
                    view.style.left = "50%"
                    view.style.transform = "translateX(-50%)"
                }
                Align.End -> view.style.right = "0px"
                Align.Fill -> view.style.width = "100%"
            }
            when (align.vertical) {
                Align.Start -> view.style.top = "0px"
                Align.Center -> {
                    view.style.top = "50%"
                    view.style.transform += " translateY(-50%)"
                }
                Align.End -> view.style.bottom = "0px"
                Align.Fill -> view.style.height = "100%"
            }
            appendLifecycled(view)
        }
    }

    override fun swap(
        view: ObservableProperty<Pair<HTMLElement, Animation>>,
        staticViewForSizing: HTMLElement?
    ): HTMLElement =
        makeElement<HTMLDivElement>("div") {
            id = "swap"
            style.maxWidth = "100%"
//            style.maxHeight = "100%"
            style.position = "relative"

            var currentView: HTMLElement? = null
            lifecycle.bind(view) { (view, animation) ->
                GlobalScope.launch(Dispatchers.UI) {
                    try {
                        removeLifecycled(currentView!!)
                    } catch (e: dynamic) {/*squish*/
                    }
                    appendLifecycled(view.apply {
                        style.width = "100%"
                        style.height = "100%"
                    })
                    currentView = view
                }
            }
        }

    override fun horizontal(vararg views: Pair<LinearPlacement, HTMLElement>): HTMLElement =
        makeElement<HTMLDivElement>("div") {
            id = "horizontal"
            style.maxWidth = "100%"
//            style.maxHeight = "100%"
            style.display = "flex"
            style.flexDirection = "row"
            for ((placement, view) in views) {
                view.style.alignSelf = placement.align.toWeb()
                view.style.flexGrow = placement.weight.toString()
                view.style.flexShrink = placement.weight.toString()
                if (placement.weight != 0f) {
                    style.width = "100%"
                }
                appendLifecycled(view)
            }
        }

    override fun vertical(vararg views: Pair<LinearPlacement, HTMLElement>): HTMLElement =
        makeElement<HTMLDivElement>("div") {
            id = "vertical"
            style.maxWidth = "100%"
//            style.maxHeight = "100%"
            style.display = "flex"
            style.flexDirection = "column"
            for ((placement, view) in views) {
                view.style.alignSelf = placement.align.toWeb()
                view.style.flexGrow = placement.weight.toString()
                view.style.flexShrink = placement.weight.toString()
                if (placement.weight != 0f) {
                    style.height = "100%"
                }
                appendLifecycled(view)
            }
        }

//    override fun contentRoot(view: HTMLElement): HTMLElement = makeElement<HTMLDivElement>("div") {
//        applyDefaultCss()
//        this.id = "root"
//        this.style.width = "100vw"
//        this.style.height = "100vh"
//        onmousemove = {
//            val event = it as MouseEvent
//            mousePosition.x = event.clientX.toFloat()
//            mousePosition.y = event.clientY.toFloat()
//            Unit
//        }
//        appendLifecycled(view)
//        this.lifecycle.alwaysOn = true
//    }
}