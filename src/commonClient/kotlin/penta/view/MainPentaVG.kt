package penta.view

import PentaViz
import com.lightningkite.koolui.color.Color
import com.lightningkite.koolui.concepts.Animation
import com.lightningkite.koolui.concepts.Importance
import com.lightningkite.koolui.concepts.TextSize
import com.lightningkite.koolui.image.Image
import com.lightningkite.koolui.image.MaterialIcon
import com.lightningkite.koolui.image.color
import com.lightningkite.koolui.image.withOptions
import com.lightningkite.koolui.views.layout.horizontal
import com.lightningkite.koolui.views.layout.space
import com.lightningkite.koolui.views.layout.vertical
import com.lightningkite.reacktive.list.mutableObservableListOf
import com.lightningkite.reacktive.property.CombineObservableProperty2
import com.lightningkite.reacktive.property.ConstantObservableProperty
import com.lightningkite.reacktive.property.StandardObservableProperty
import com.lightningkite.reacktive.property.transform
import com.lightningkite.recktangle.Point
import io.data2viz.geom.svgPath
import io.data2viz.viz.PathNode
import penta.view.test.CanvasTestVG

class MainPentaVG<VIEW>() : MyViewGenerator<VIEW> {
//    override val title: String = "KotlinX UI Test"

    val views = mutableObservableListOf<Triple<String, MaterialIcon, MyViewGenerator<VIEW>>>(
        Triple("Rules", MaterialIcon.help, RulesVG<VIEW>()),
        Triple("Notation", MaterialIcon.history, HistoryVG<VIEW>()),
        Triple("Multiplayer", MaterialIcon._public, MultiplayerVG<VIEW>()),
        Triple("Canvas Test", MaterialIcon.lineStyle, CanvasTestVG<VIEW>())
    )
    val selectedIconIndex = StandardObservableProperty(0)

    init {
        //Startup
    }

    override fun generate(dependency: MyViewFactory<VIEW>): VIEW = with(dependency) {
        val mainSwapContent = StandardObservableProperty(
            views.first().third.generate(this) to Animation.Fade
        )
        horizontal {
            // left
            +vizCanvas(ConstantObservableProperty(PentaViz.viz))
            // right
            +vertical {
                // top bar
                -horizontal {
                    +space()
                    -swap(
                        view = CombineObservableProperty2(
                            PentaViz.gameState.players.onListUpdate,
                            PentaViz.gameState.currentPlayerProperty
                        ) { players, currentPlayer ->
                            horizontal {
                                +space()
                                players.forEach { player ->
                                    val svgImage = player.figureIdProperty.transform { figureId ->
                                        val pathNode = PathNode()
                                        with(PentaViz) {
                                            pathNode.drawPlayer(
                                                figureId,
                                                center = io.data2viz.geom.Point(12.0, 12.0),
                                                radius = 12.0
                                            )
                                        }
                                        val svgString = pathNode.path.svgPath.let {
                                            """<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="$it"/></svg>"""
                                        }
                                        val color = if (currentPlayer.id == player.id) Color.white else Color.black
                                        Image.fromSvgString(
                                            svgString.substringBefore('g') + """g fill="${color.toAlphalessWeb()}"""" + svgString.substringAfter(
                                                'g'
                                            )
                                        ).withOptions(
                                            Point(24f, 24f)
                                        )
                                    }
                                    val imageButon = imageButton(
                                        imageWithOptions = svgImage,
                                        label = ConstantObservableProperty(player.id),
                                        importance = if (currentPlayer.id == player.id) Importance.Normal else Importance.Low,
                                        onClick = {
                                            // TODO: show popup / dropdown
                                        }
                                    ).setHeight(48f).setWidth(48f)

                                    -imageButon
//                                -entryContext(
//                                    label = player.id,
//                                    field = imageButon
//                                ).setWidth(64f)
                                }
                            } to Animation.Fade
                        }
                    )
                    +space()
                    -text(
                        text = PentaViz.turnDisplay,
                        size = TextSize.Body.bigger
                    )
                }.setHeight(64f)
                // middle
                +horizontal {
                    // main content
                    +swap(
                        view = mainSwapContent
                    )
                    // right bar
                    -swap(
                        view = CombineObservableProperty2(
                            selectedIconIndex,
                            views.onListUpdate
                        ) { selectedIndex, list ->
                            vertical {
                                list.forEachIndexed { index, (label, icon, vg) ->
                                    val imageButton = imageButton(
                                        imageWithOptions = ConstantObservableProperty(
                                            icon.color(if (index == selectedIndex) Color.white else Color.gray).withOptions(
                                                Point(24f, 24f)
                                            )
                                        ),
                                        label = ConstantObservableProperty(label),
                                        importance = if (index == selectedIndex) Importance.Normal else Importance.Low,
                                        onClick = {
                                            selectedIconIndex.value = index
                                            mainSwapContent.value = vg.generate(dependency) to Animation.Fade
//                                            stack.push(createViewGenerator.invoke())
                                        }
                                    ).setHeight(32f)
                                    -imageButton
//                                    -entryContext(
//                                        label = label,
//                                        field = imageButton
//                                    ).setWidth(32f)
                                }
                            } to Animation.Fade
                        }
                    )
                }
            }//.setWidth(480f)
        }.growVertical(dependency)
    }
}

private fun <VIEW> VIEW.growVertical(dependency: MyViewFactory<VIEW>): VIEW = with(dependency) {
    vertical {
        +this@growVertical
    }
}
