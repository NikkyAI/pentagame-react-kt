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
import com.lightningkite.koolui.views.basic.text
import com.lightningkite.koolui.views.layout.horizontal
import com.lightningkite.koolui.views.layout.space
import com.lightningkite.koolui.views.layout.vertical
import com.lightningkite.reacktive.list.mutableObservableListOf
import com.lightningkite.reacktive.property.CombineObservableProperty2
import com.lightningkite.reacktive.property.ConstantObservableProperty
import com.lightningkite.reacktive.property.MutableObservableProperty
import com.lightningkite.reacktive.property.StandardObservableProperty
import com.lightningkite.reacktive.property.sub
import com.lightningkite.reacktive.property.transform
import com.lightningkite.recktangle.Point
import io.data2viz.geom.svgPath
import io.data2viz.viz.PathNode
import mu.KotlinLogging
import penta.ClientGameState
import penta.PlayerState
import penta.util.asKoolUIColor
import penta.util.fromSvgString

class MainPentaVG<VIEW>() : MyViewGenerator<VIEW> {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
//    override val title: String = "KotlinX UI Test"

    val views = mutableObservableListOf<Triple<String, MaterialIcon, MyViewGenerator<VIEW>>>(
        Triple("Rules", MaterialIcon.help, RulesVG()),
        Triple("Notation", MaterialIcon.history, HistoryVG()),
        Triple("Multiplayer", MaterialIcon._public, MultiplayerVG()),
        Triple("Settings", MaterialIcon.settings, SettingsVG()),
        Triple("About", MaterialIcon.info, AboutVG()),
        Triple("Tests", MaterialIcon.developerMode, ExperimentsVG())
    )
    val selectedIconIndex = StandardObservableProperty(0)

    init {
        //Startup
    }

    fun MyViewFactory<VIEW>.drawPlayer(gameState: ClientGameState, player: PlayerState, isCurrent: Boolean) = vertical {
        val svgImage = player.figureIdProperty.transform { figureId ->
            val pathNode = PathNode()
            with(PentaViz) {
                pathNode.drawPlayer(
                    figureId,
                    center = io.data2viz.geom.Point(12.0, 12.0),
                    radius = 12.0
                )
            }
            val color = if (isCurrent) Color.white else Color.black
            val svgString = pathNode.path.svgPath.let {
                """<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="$it"/></svg>"""
            }
            Image.fromSvgString(svgString, color)
                .withOptions(
                    Point(24f, 24f)
                )
        }
        -imageButton(
            imageWithOptions = svgImage,
            label = ConstantObservableProperty(player.id),
            importance = if (isCurrent) Importance.Normal else Importance.Low,
            onClick = {
                //TODO: find the damn dialog too ?
                launchDialog(true,
                    {
                        logger.info { "onDismiss" }
                    },
                    {
                        logger.info { "onClick" }
                        frame(
                            vertical {
                                text("a")
                                text("b")
                                text("c")
                                text("d")
                            }
                        )

                    }
                )
                // TODO: show popup / dropdown for changing face
            }
        ).setHeight(48f).setWidth(48f)
        fun scoreColor(i: Int) = gameState.scoringColors.onMapUpdate.transform {
            val colors = it[player.id] ?: emptyList()
            colors.getOrNull(i)?.color?.asKoolUIColor() ?: ConstantObservableProperty(Color.gray.toWhite(0.5f))
        }.sub { it }
        -horizontal {
            repeat(3) { i ->
                -space()
                +space(20f, 10f).background(scoreColor(i))
                -space()
            }
        }
    }.setHeight(64f)

    fun MyViewFactory<VIEW>.generateTopBar() = swap(
        PentaViz.gameStateProperty.transform { gameState ->
            logger.info { "generating top bar" }
            horizontal {
                +space()
                -swap(
                    view = CombineObservableProperty2(
                        gameState.players.onListUpdate,
                        gameState.currentPlayerProperty
                    ) { players, currentPlayer ->
                        horizontal {
                            +space()
                            players.forEach { player ->
                                -drawPlayer(gameState, player, currentPlayer.id == player.id)
//                                -entryContext(
//                                    label = player.id,
//                                    field = imageButon
//                                ).setWidth(64f)
                            }
                        } to Animation.Fade
                    }
                ).setHeight(64f)
                +space()
                -text(
                    text = gameState.turnProperty.transform { "Turn: $it" },
                    size = TextSize.Body.bigger.bigger
                )
            }.setHeight(64f) to Animation.Fade
        }
    ).setHeight(64f)

    fun MyViewFactory<VIEW>.generateRightBar(mainSwapContent: MutableObservableProperty<Pair<VIEW, Animation>>) = swap(
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
                            mainSwapContent.value = vg.generate(this@generateRightBar) to Animation.Fade
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
    ).also {
        selectedIconIndex.value = 0
    }

    override fun generate(dependency: MyViewFactory<VIEW>): VIEW = with(dependency) {
        val mainSwapContent = StandardObservableProperty(
            views.first().third.generate(this) to Animation.Fade
//            space() to Animation.Fade
        )
        horizontal {
            // left
            +vizCanvas(ConstantObservableProperty(PentaViz.viz))
            // right
            +vertical {
                // top bar
                -generateTopBar().setHeight(64f)
                // middle
                +horizontal {
                    // main content
                    +swap(
                        view = mainSwapContent
                    )
                    // right bar
                    -generateRightBar(mainSwapContent).setWidth(64f)
                }
            }
        }.growVertical(dependency)
    }
}

private fun <VIEW> VIEW.growVertical(dependency: MyViewFactory<VIEW>): VIEW = with(dependency) {
    vertical {
        +this@growVertical
    }
}
