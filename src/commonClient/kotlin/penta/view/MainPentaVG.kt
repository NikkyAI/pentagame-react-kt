package penta.view

import PentaViz
import com.lightningkite.kommon.collection.push
import com.lightningkite.koolui.views.layout.*
import com.lightningkite.koolui.color.Color
import com.lightningkite.koolui.concepts.Importance
import com.lightningkite.koolui.concepts.TabItem
import com.lightningkite.koolui.geometry.Direction
import com.lightningkite.koolui.geometry.LinearPlacement
import com.lightningkite.koolui.image.Image
import com.lightningkite.koolui.image.MaterialIcon
import com.lightningkite.koolui.image.color
import com.lightningkite.koolui.image.withOptions
import com.lightningkite.reacktive.list.MappingObservableList
import com.lightningkite.reacktive.list.StandardObservableList
import com.lightningkite.reacktive.list.WrapperObservableList
import com.lightningkite.reacktive.list.asObservableList
import com.lightningkite.reacktive.list.mutableObservableListOf
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
    val mainView = RulesVG<VIEW>()
//    val mainView = IconsTestVG<VIEW>() // SelectorVG(stack)

    val views = mutableObservableListOf<Triple<String, MaterialIcon, () -> MyViewGenerator<VIEW>>>(
        Triple("Rules", MaterialIcon.help, { RulesVG<VIEW>() }),
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
    val tabOptions = views.map {(label, icon, createViewGenerator) ->
        TabItem(
            imageWithOptions =  icon.color(Color.gray).withOptions(
                Point(24f, 24f)
            ),
            text = label,
            description = "description"
        )
    }.asObservableList()

    val tabSelection = StandardObservableProperty(tabOptions.first()).apply {
        add {
            println("tab selected: $it")
        }
    }

    init {
        //Startup
        stack.push(views.first().third.invoke())
    }

    override fun generate(dependency: MyViewFactory<VIEW>): VIEW = with(dependency) {
        horizontal {
            +vertical {
                -horizontal {
                    -text(PentaViz.turnDisplay)
                    +list(
                        data = PentaViz.gameState.players,
                        direction = Direction.Right,
                        makeView = { itemObs, index ->
                            val player = itemObs.value
                            val svgImage = itemObs.transform {
                                val pathNode = PathNode()
                                with(PentaViz) {
                                    pathNode.drawPlayer(
                                        it.figureId,
                                        center = io.data2viz.geom.Point(12.0, 12.0),
                                        radius = 12.0
                                    )
                                }
                                val svgString = pathNode.path.svgPath.let {
                                    """<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="$it"/></svg>"""
                                }
                                val color = Color.black
                                Image.fromSvgString(
                                    svgString.substringBefore('g') + """g fill="${color.toAlphalessWeb()}"""" + svgString.substringAfter(
                                        'g'
                                    )
                                ).withOptions(
                                    Point(24f, 24f)
                                )
                            }

                            entryContext(
                                label = player.id,
                                field = image(svgImage)
                            )
                        }
                    )
                }
                +horizontal(
                    LinearPlacement.wrapStart to list(
                        data = views,
                        makeView = { itemObs, index ->
                            val (label, icon, createViewGenerator) = itemObs.value
                            entryContext(
                                label = label,
                                field = imageButton(
                                    imageWithOptions = itemObs.transform { (label, icon, createViewGenerator) ->
                                        icon.color(Color.gray).withOptions(
                                            Point(24f, 24f)
                                        )
                                    },
                                    label = itemObs.transform { (label, icon, createViewGenerator) ->
                                        "$label Button"
                                    },
                                    importance = Importance.Normal,
                                    onClick = {
                                        stack.push(createViewGenerator.invoke())
                                    }
                                )
                            )
                        }
                    ),
//                LinearPlacement.wrapStart to tabs(
//                    tabOptions,
//                    tabSelection
//                ),
                    LinearPlacement.fillCenter to window(
                        dependency = dependency,
                        stack = stack,
                        tabs = listOf()
                    )
                )
            }
            val playerSymbols = listOf("triangle", "square", "cross", "circle")
            val playerCount = 3
            +vizCanvas(ConstantObservableProperty(PentaViz.viz)) {
                with(PentaViz) {
                    it.addEvents()
                }
                PentaViz.gameState.initialize(playerSymbols.subList(0, playerCount))
            }
        }
    }
}