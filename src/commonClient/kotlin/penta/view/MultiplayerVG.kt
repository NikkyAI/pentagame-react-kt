package penta.view

import PentaViz
import client
import com.lightningkite.koolui.concepts.Animation
import com.lightningkite.koolui.concepts.Importance
import com.lightningkite.koolui.concepts.TextInputType
import com.lightningkite.koolui.concepts.TextSize
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
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.content.TextContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KLogger
import mu.KotlinLogging
import penta.LoginState
import penta.json
import penta.network.LoginRequest
import penta.network.LoginResponse
import penta.network.ServerStatus
import penta.util.authenticateWith
import penta.util.info
import penta.util.parse

class MultiplayerVG<VIEW>() : MyViewGenerator<VIEW> {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    fun login(urlInput: String, userIdInput: String, passwordInput: String?) {
        val baseURL = Url(urlInput)
        login(baseURL, userIdInput, passwordInput)
    }

    fun login(baseURL: Url, userIdInput: String, passwordInput: String?) {
        GlobalScope.launch(Dispatchers.Default) {
            val url =  URLBuilder(baseURL).apply {
                path("api", "status")
            }.build()
            logger.info {"url: $url"}
            val status = try {
                client.request<ServerStatus>(url) {}
            } catch (exception: Exception) {
                logger.error(exception) { "request failed" }
                null
            }

            logger.info {"status: $status"}
            if (status != null) {
                val loginUrl = URLBuilder(baseURL).apply {
                    path("api", "login")
                }.build()
                val (loginResponse, sessionId) = client.post<HttpResponse>(loginUrl) {
                    body = TextContent(
                        text = json.stringify(
                            LoginRequest.serializer(),
                            LoginRequest(
                                userId = userIdInput,
                                password = passwordInput
                            )
                        ),
                        contentType = ContentType.Application.Json
                    )
                }.run {
                    logger.debug { "headers: $headers"}
                    parse(LoginResponse.serializer()) to headers["SESSION"]
                }
                PentaViz.gameState.multiplayerState.value = when(loginResponse) {
                    is LoginResponse.UserIdRejected -> {
                        LoginState.UserIDRejected(
                            userId = userIdInput,
                            reason = loginResponse.reason
                        )
                    }
                    is LoginResponse.IncorrectPassword -> LoginState.RequiresPassword(
                        baseUrl = baseURL,
                        userId = userIdInput
                    )
                    is LoginResponse.Success -> LoginState.Connected(
                        baseUrl = baseURL,
                        userId = userIdInput,
                        session = sessionId ?: throw IllegalStateException("missing SESSION header")
                    ).also { state ->
                        val whoAmIUrl = URLBuilder(baseURL).apply {
                            path("whoami")
                        }.build()
                       client.get<HttpResponse>(whoAmIUrl) {
                            authenticateWith(state)
                        }.run {
                            headers["SESSION"]?.let {
                                state.session = it
                            }
                           readText().info(logger)
                        }
                    }
                }
            }
        }
    }

    override fun generate(dependency: MyViewFactory<VIEW>): VIEW = with(dependency) {
        swap(
            PentaViz.gameState.multiplayerState.transform { state ->
                when (state) {
                    is LoginState.Disconnected, is LoginState.UserIDRejected -> {
                        val urlInput = StandardObservableProperty("http://127.0.0.1:55555")
                        val userIdInput = StandardObservableProperty(
                            (state as? LoginState.UserIDRejected)?.run { userId } ?: ""
                        )

                        vertical {
                            +space()
                            if(state is LoginState.UserIDRejected) {
                                -card(text(
                                    text = state.reason,
                                    size = TextSize.Body.bigger,
                                    importance = Importance.Danger
                                ))//.background(theme.importance(Importance.Danger).background)
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
                                        login(urlInput.value, userIdInput.value, null)
                                    }
                                )
                            }.setHeight(32f)
                            +space()
                        } to Animation.Fade
                    }
                    is LoginState.RequiresPassword -> {
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
                                        login(state.baseUrl, state.userId, passwordInput.value)
                                    }
                                )
                            }.setHeight(32f)
                            +space()

                        } to Animation.Fade
                    }
                    is LoginState.Connected -> {
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
                                            PentaViz.gameState.multiplayerState.value = LoginState.Disconnected
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



