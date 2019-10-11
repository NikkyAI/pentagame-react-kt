package penta.view

import PentaViz
import com.lightningkite.kommon.collection.push
import com.lightningkite.koolui.color.Color
import com.lightningkite.koolui.concepts.Animation
import com.lightningkite.koolui.concepts.Importance
import com.lightningkite.koolui.image.Image
import com.lightningkite.koolui.image.MaterialIcon
import com.lightningkite.koolui.image.color
import com.lightningkite.koolui.image.withOptions
import com.lightningkite.koolui.views.layout.horizontal
import com.lightningkite.koolui.views.layout.vertical
import com.lightningkite.reacktive.list.WrapperObservableList
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

    val stack = WrapperObservableList<MyViewGenerator<VIEW>>()

    val views = mutableObservableListOf<Triple<String, MaterialIcon, () -> MyViewGenerator<VIEW>>>(
        Triple("Rules", MaterialIcon.help, { RulesVG<VIEW>() }),
        Triple("Notation", MaterialIcon.history, { HistoryVG<VIEW>() }),
        Triple("Canvas Test", MaterialIcon.lineStyle, { CanvasTestVG<VIEW>() })
//        Triple("Space Test", MaterialIcon.add, { SpaceTestVG<VIEW>() }),
//        Triple("Original Test", MaterialIcon.add, { OriginalTestVG<VIEW>() }),
//        Triple("Alpha", MaterialIcon.add, { AlphaTestVG<VIEW>() }),
//        Triple("Horizontal", MaterialIcon.add, { HorizontalVG<VIEW>() }),
//        Triple("Vertical", MaterialIcon.add, { VerticalTestVG<VIEW>() }),
//        Triple("Pages", MaterialIcon.add, { PagesVG<VIEW>() }),
//        Triple("Frame", MaterialIcon.add, { FrameVG<VIEW>() }),
////            "Http Call Test" to { HttpCallTestVG<VIEW>() }),
//        Triple("Controls", MaterialIcon.add, { ControlsVG<VIEW>() }),
//        Triple("Notifications", MaterialIcon.add, { NotificationTestVG<VIEW>() }),
//        Triple("Icons", MaterialIcon.add, { IconsTestVG<VIEW>() }),
////            "URL Image Test" to { UrlImageTestVG<VIEW>() },
//        Triple("Dialog", MaterialIcon.add, { DialogTestVG<VIEW>() })
    )
    val selectedIconIndex = StandardObservableProperty(0)

    init {
        //Startup
        stack.push(views.first().third.invoke())
    }

    override fun generate(dependency: MyViewFactory<VIEW>): VIEW = with(dependency) {
        val mainSwapContent = StandardObservableProperty(
            views.first().third.invoke().generate(this) to Animation.Fade
        )
        horizontal {
            +vertical {
                -horizontal {
                    -text(PentaViz.turnDisplay)
                    +swap(view = CombineObservableProperty2(
                        PentaViz.gameState.players.onListUpdate,
                        PentaViz.gameState.currentPlayerProperty
                    ) { players, currentPlayer ->
                        horizontal {
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

                                -entryContext(
                                    label = player.id,
                                    field = imageButton(
                                        imageWithOptions = svgImage,
                                        importance = if (currentPlayer.id == player.id) Importance.Normal else Importance.Low,
                                        onClick = {
                                            // TODO: show popup / dropdown
                                        }
                                    ).setHeight(32f)
                                ).setWidth(64f)

                            }

                        } to Animation.Fade
                    }
                    )
                }.setHeight(48f)
                +horizontal {
                    -swap(
                        view = CombineObservableProperty2(
                            selectedIconIndex,
                            views.onListUpdate
                        ) { selectedIndex, list ->
                            vertical {
                                list.forEachIndexed { index, (label, icon, createViewGenerator) ->
                                    val imageButton = imageButton(
                                        imageWithOptions = ConstantObservableProperty(
                                            icon.color(if (index == selectedIndex) Color.white else Color.gray).withOptions(
                                                Point(24f, 24f)
                                            )
                                        ),
                                        label = ConstantObservableProperty(
                                            "$label Button"
                                        ),
                                        importance = if (index == selectedIndex) Importance.Normal else Importance.Low,
                                        onClick = {
                                            selectedIconIndex.value = index
                                            mainSwapContent.value =
                                                createViewGenerator.invoke().generate(dependency) to Animation.Fade
//                                            stack.push(createViewGenerator.invoke())
                                        }
                                    ).setHeight(32f)
                                    -imageButton
//                                    -entryContext(
//                                        label = label,
//                                        field = imageButton
//                                    )
                                }
                            }.setWidth(32f) to Animation.Fade
                        }
                    )
                    +swap(
                        view = mainSwapContent
                    )
//                    +window(
//                        dependency = dependency,
//                        stack = stack,
//                        tabs = listOf()
//                    )
                }
            }

            +vizCanvas(ConstantObservableProperty(PentaViz.viz))
        }
    }
}