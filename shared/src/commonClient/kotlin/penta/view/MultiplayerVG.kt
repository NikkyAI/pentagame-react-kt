package penta.view

import PentaViz
import com.lightningkite.koolui.async.UI
import com.lightningkite.koolui.concepts.Animation
import com.lightningkite.koolui.concepts.Importance
import com.lightningkite.koolui.concepts.TextInputType
import com.lightningkite.koolui.concepts.TextSize
import com.lightningkite.koolui.views.basic.text
import com.lightningkite.koolui.views.basic.work
import com.lightningkite.koolui.views.interactive.button
import com.lightningkite.koolui.views.layout.horizontal
import com.lightningkite.koolui.views.layout.space
import com.lightningkite.koolui.views.layout.vertical
import com.lightningkite.reacktive.list.mutableObservableListOf
import com.lightningkite.reacktive.property.ConstantObservableProperty
import com.lightningkite.reacktive.property.StandardObservableProperty
import com.lightningkite.reacktive.property.transform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import penta.ConnectionState
import penta.WSClient
import penta.network.GameSessionInfo
import penta.util.handler

class MultiplayerVG<VIEW>() : MyViewGenerator<VIEW> {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun generate(dependency: MyViewFactory<VIEW>): VIEW = with(dependency) {
        swap(PentaViz.gameStateProperty.transform { gameState ->
            swap(
                PentaViz.multiplayerState.transform { state ->
                    when (state) {
                        is ConnectionState.Disconnected, is ConnectionState.UserIDRejected -> {
                            val urlInput = StandardObservableProperty(state.baseUrl.toString())
                            val userIdInput = StandardObservableProperty(state.userId)

                            vertical {
                                +space()
                                if (state is ConnectionState.UserIDRejected) {
                                    -card(
                                        text(
                                            text = state.reason,
                                            size = TextSize.Body.bigger,
                                            importance = Importance.Danger
                                        )
                                    )//.background(theme.importance(Importance.Danger).background)
                                }
                                -text("Username:")
                                -textField(
                                    text = userIdInput,
                                    type = TextInputType.Name
                                ).background(theme.main.background)
                                -text("Enter Server URL:")
                                -horizontal {
                                    +textField(
                                        text = urlInput,
                                        placeholder = "localhost",
                                        type = TextInputType.URL
                                    ).background(theme.main.background)
                                    -button(
                                        label = "Connect",
                                        onClick = {
                                            GlobalScope.launch(Dispatchers.UI + handler) {
                                                WSClient.login(urlInput.value, userIdInput.value, null)
                                            }
                                        }
                                    )
                                }.setHeight(32f)
                                +space()
                            } to Animation.Fade
                        }
                        is ConnectionState.RequiresPassword -> {
                            val passwordInput = StandardObservableProperty("")

                            vertical {
                                +space()
                                -text("Enter password")
                                -horizontal {
                                    +textField(
                                        text = passwordInput,
                                        type = TextInputType.Password
                                    ).background(theme.main.background)
                                    -button(
                                        label = "Login",
                                        onClick = {
                                            GlobalScope.launch(Dispatchers.UI + handler) {
                                                WSClient.login(state.baseUrl, state.userId, passwordInput.value)
                                            }
                                        }
                                    )
                                }.setHeight(32f)
                                -button(
                                    label = "back",
                                    onClick = {
                                        PentaViz.multiplayerState.value = ConnectionState.Disconnected(
                                            baseUrl = state.baseUrl,
                                            userId = state.userId
                                        )
                                    }
                                )
                                +space()

                            } to Animation.Fade
                        }
                        is ConnectionState.Lobby -> {
                            //TODO: receive games list initially
                            val games = mutableObservableListOf<GameSessionInfo>()
                            games.onListUpdate.add {
                                logger.info { "list updated: ${it.joinToString()}" }
                            }
                            GlobalScope.launch(Dispatchers.UI + handler) {
                                WSClient.listGames(state, games)
                            }
                            val refreshing = StandardObservableProperty(false)

                            refresh(
                                contains = vertical {
                                    -horizontal {
                                        +text(
                                            "Connected with ${state.baseUrl}"
                                        )
                                        -button(
                                            label = "Disconnect",
                                            onClick = {
                                                PentaViz.multiplayerState.value = ConnectionState.Disconnected(
                                                    baseUrl = state.baseUrl,
                                                    userId = state.userId
                                                )
                                            }
                                        )
                                    }
                                    -horizontal {
                                        -button(
                                            label = "Create Game",
                                            onClick = {
                                                GlobalScope.launch(Dispatchers.UI + handler) {
                                                    WSClient.createGameAndConnect(state)
                                                }
                                            }
                                        )
                                        +space()
                                    }
                                    +list(
                                        data = games,
                                        makeView = { obs, _ ->
                                            val game = obs.value
                                            game.run {
                                                horizontal {
                                                    -vertical {
                                                        -text("id: $id")
                                                        -text("running: $running")
                                                    }
                                                    +space()
                                                    -vertical {
                                                        +text("owner: ${game.owner}")
                                                        +text("running: ${game.running}")
                                                    }
                                                    +space()
                                                    -vertical {
                                                        +text("players: ${players.size}")
                                                        +text("observers: ${observers.size}")
                                                    }
                                                    -button(
                                                        label = "Join",
                                                        onClick = {
                                                            GlobalScope.launch(Dispatchers.UI + handler) {
                                                                WSClient.connectToGame(state, game)
                                                            }
                                                        }
                                                    )
                                                }
                                            }

                                        }
                                    )
                                },
                                working = refreshing,
                                onRefresh = {
                                    refreshing.value = true

                                    // TODO: receive fresh game list from server
                                    GlobalScope.launch(Dispatchers.UI + handler) {
                                        WSClient.listGames(state, games) {
                                            refreshing.value = false
                                        }
                                    }
                                }
                            ).setWidth(200f) to Animation.Fade
                        }
                        is ConnectionState.Observing -> {
//                            gameState.currentPlayerProperty.add {
//                                if (it.id == state.userId) {
//                                    logger.info { "showing notification" }
//                                    if (!gameState.isPlayback) {
//                                        showNotification(
//                                            "Your Turn",
//                                            "Last Move: " + gameState.history.last().asNotation()
//                                        )
//                                    }
//                                }
//                            }
                            vertical {
                                -text("gameId: ${state.game.id}")
                                -text("owner: ${state.game.owner}")
//                                -text(gameState.observersProperty.onListUpdate.transform {
//                                    "connected: ${it.joinToString()}"
//                                })
                                // TODO: update list of connected observers
                                // TODO: chat ?
//                                -swap(
//                                    CombineObservableProperty2(
//                                        gameState.gameStartedProperty,
//                                        gameState.players.onListUpdate
//                                    ) { initialized, players ->
//                                        if (!initialized && players.none { it.id == state.userId }) {
//                                            button(
//                                                label = "Join",
//                                                onClick = {
//                                                    GlobalScope.launch(Dispatchers.UI + handler) {
//                                                        WSClient.joinGame(state)
//                                                    }
//                                                }
//                                            )
//                                        } else {
//                                            // TODO: add "spectate" ?
//                                            space()
//                                        } to Animation.Fade
//                                    }
//                                )
                                +space()
                                // TODO: move to top row instead
//                                -swap(gameState.gameStartedProperty.transform {
//                                    if (!it && state.game.owner == state.userId) {
//                                        button(
//                                            // TODO: hide once started
//                                            label = "Start",
//                                            onClick = {
//                                                GlobalScope.launch(Dispatchers.UI + handler) {
//                                                    WSClient.startGame(state)
//                                                }
//                                            }
//                                        )
//                                    } else {
//                                        space()
//                                    } to Animation.Fade
//                                })
                                +space()
                                -button(
                                    label = "Leave Game",
                                    onClick = {
                                        GlobalScope.launch(Dispatchers.UI + handler) {
                                            state.leave()
                                        }
                                    }
                                )
                            } to Animation.Fade
                        }
                        is ConnectionState.Authenticated -> {
                            vertical {
                                +space()
                                -work(text("Connecting"), ConstantObservableProperty(true))
                                +space()
                            } to Animation.Fade
                        }
                    }
                }
            ) to Animation.Fade
        })
    }
}
