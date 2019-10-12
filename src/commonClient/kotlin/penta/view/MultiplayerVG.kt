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
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.content.TextContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import penta.ConnectionState
import penta.json
import penta.network.LoginRequest
import penta.network.LoginResponse
import penta.network.ServerStatus

class MultiplayerVG<VIEW>() : MyViewGenerator<VIEW> {
    fun login(urlInput: String, userIdInput: String, passwordInput: String?) {
        val baseURL = Url(urlInput)
        login(baseURL, userIdInput, passwordInput)
    }

    fun login(baseURL: Url, userIdInput: String, passwordInput: String?) {
        GlobalScope.launch(Dispatchers.Default) {
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
                val loginUrl = URLBuilder(baseURL).apply {
                    path("api", "login")
                }.build()
                val loginResponse = client.post<LoginResponse>(loginUrl) {
                    body = TextContent(
                        text = json.stringify(
                            LoginRequest.serializer(),
                            LoginRequest(
                                userId = userIdInput,
                                password = passwordInput
                            )
                        ),
                        contentType = ContentType.Application.Json
                    ).also {
                        println("posting body: ${it.text}")
                    }
                }
                PentaViz.gameState.multiplayerState.value = when(loginResponse) {
                    is LoginResponse.Success -> ConnectionState.Connected(
                        baseUrl = baseURL,
                        userId = userIdInput)
                    is LoginResponse.IncorrectPassword -> ConnectionState.RequiresPassword(
                        baseUrl = baseURL,
                        userId = userIdInput
                    )
                }
            }
        }
    }

    override fun generate(dependency: MyViewFactory<VIEW>): VIEW = with(dependency) {
        swap(
            PentaViz.gameState.multiplayerState.transform { state ->
                when (state) {
                    is ConnectionState.Disconnected -> {
                        val urlInput = StandardObservableProperty("http://127.0.0.1:55555")
                        val userIdInput = StandardObservableProperty("")

                        vertical {
                            +space()
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
                                        login(urlInput.value, userIdInput.value, null)
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
                                    label = "Connect 2",
                                    onClick = {
                                        login(state.baseUrl, state.userId, passwordInput.value)
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

                        refresh(
                            contains = vertical {
                                -horizontal {
                                    +text(
                                        "Connected with ${state.baseUrl}"
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
                                        horizontal {
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
                        ).setWidth(200f) to Animation.Fade
                    }
                }
            }
        )
    }
}