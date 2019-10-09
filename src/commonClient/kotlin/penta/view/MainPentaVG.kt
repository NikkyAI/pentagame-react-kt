package penta.view

import PentaViz
import com.lightningkite.kommon.collection.push
import com.lightningkite.kommon.collection.reset
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
    val stack = WrapperObservableList<MyViewGenerator<VIEW>>()
    val mainView = RulesVG<VIEW>()

    val views = mutableObservableListOf(
        Triple("Rules", MaterialIcon.help, { RulesVG<VIEW>() }),
        Triple("Canvas Test", MaterialIcon.lineStyle, { CanvasTestVG<VIEW>() })
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
                +horizontal {
                    -list(
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
                                        stack.reset(createViewGenerator.invoke())
                                    }
                                )
                            )
                        }
                    )
                    +window(
                        dependency = dependency,
                        stack = stack,
                        tabs = listOf()
                    )
                }
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