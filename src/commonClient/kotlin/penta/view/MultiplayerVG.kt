package penta.view

import PentaViz
import client
import com.lightningkite.koolui.concepts.Animation
import com.lightningkite.koolui.concepts.TextInputType
import com.lightningkite.koolui.views.basic.text
import com.lightningkite.koolui.views.interactive.button
import com.lightningkite.koolui.views.layout.horizontal
import com.lightningkite.koolui.views.layout.space
import com.lightningkite.koolui.views.layout.vertical
import com.lightningkite.reacktive.list.StandardObservableList
import com.lightningkite.reacktive.list.mutableObservableListOf
import com.lightningkite.reacktive.property.StandardObservableProperty
import com.lightningkite.reacktive.property.transform
import com.lightningkite.reacktive.property.update
import io.ktor.client.request.request
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import penta.ConnectionState
import penta.network.ServerStatus

class MultiplayerVG<VIEW>() : MyViewGenerator<VIEW> {
    fun connect(urlInput: String) {
        GlobalScope.launch {
            val baseURL = Url(urlInput)
            val url =  URLBuilder(baseURL).apply {
                path("api", "status")
            }.build()
            println("url: $url")
            val status = try {
                client.request<ServerStatus>(url) { }
            } catch (exception: Exception) {
                println("Exception: " + exception.message)
                null
            }

            println("status: $status")
            if (status != null) {
                PentaViz.gameState.multiplayerState.value = ConnectionState.Connected(baseURL)
            }
        }
    }

    override fun generate(dependency: MyViewFactory<VIEW>): VIEW = with(dependency) {
        swap(
            PentaViz.gameState.multiplayerState.transform {
                when (it) {
                    is ConnectionState.Disconnected -> {
                        val textInput = StandardObservableProperty("http://127.0.0.1:55555")

                        vertical {
                            +space()
                            -text("Enter Server URL:")
                            -horizontal {
                                +textField(
                                    text = textInput,
                                    placeholder = "localhost",
                                    type = TextInputType.URL
                                ).background(theme.main.background)
                                -button(
                                    label = "Connect",
                                    onClick = {
                                        connect(textInput.value)
                                    }
                                )
                            }.setHeight(32f)
                            +space()
                        } to Animation.Fade
                    }
                    is ConnectionState.Connected -> {
                        //TODO: receive games list initially
                        val games = mutableObservableListOf<String>()
                        val gameRows = StandardObservableList<List<String>>()
                        games.onListUpdate.add {
                            gameRows.replace(games.chunked(5))
                        }
                        val refreshing = StandardObservableProperty(false)

                        card(refresh(
                            contains = vertical {
                                -horizontal {
                                    +text(
                                        "Connected with ${it.url}"
                                    )
                                    -button(
                                        label = "Disconnect",
                                        onClick = {
                                            PentaViz.gameState.multiplayerState.value = ConnectionState.Disconnected
                                        }
                                    )
                                }
                                +list(
                                    data = gameRows,
                                    makeView = { listProp, indexProp ->
                                        vertical {
                                            listProp.value.forEach { gameLabel ->
                                                +card(text(gameLabel))
                                            }
                                        }
                                    }
                                )
                            },
                            working = refreshing,
                            onRefresh = {
                                refreshing.value = true

                                // TODO: receive fresh game list from server
                                games.replace(
                                    (0..60).map { ('A' + it).toString() }
                                )

                                refreshing.value = false
                                refreshing.update()
                            }
                        )) to Animation.Fade
                    }
                }
            }
        )
    }
}