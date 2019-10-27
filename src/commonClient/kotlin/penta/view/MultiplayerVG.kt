package penta.view

import PentaViz
import client
import com.lightningkite.koolui.async.UI
import com.lightningkite.koolui.concepts.Animation
import com.lightningkite.koolui.concepts.Importance
import com.lightningkite.koolui.concepts.TextInputType
import com.lightningkite.koolui.concepts.TextSize
import com.lightningkite.koolui.views.basic.text
import com.lightningkite.koolui.views.interactive.button
import com.lightningkite.koolui.views.layout.horizontal
import com.lightningkite.koolui.views.layout.space
import com.lightningkite.koolui.views.layout.vertical
import com.lightningkite.reacktive.list.MutableObservableList
import com.lightningkite.reacktive.list.mutableObservableListOf
import com.lightningkite.reacktive.property.CombineObservableProperty2
import com.lightningkite.reacktive.property.StandardObservableProperty
import com.lightningkite.reacktive.property.transform
import io.ktor.client.features.websocket.webSocket
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.content.TextContent
import io.ktor.http.fullPath
import io.ktor.http.setCookie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.list
import mu.KotlinLogging
import penta.ClientGameState
import penta.MultiplayerState
import penta.SerialNotation
import penta.json
import penta.network.GameSessionInfo
import penta.network.LoginRequest
import penta.network.LoginResponse
import penta.network.ServerStatus
import penta.util.authenticateWith
import penta.util.authenticatedRequest
import penta.util.parse
import penta.util.suspendInfo

class MultiplayerVG<VIEW>() : MyViewGenerator<VIEW> {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun status(
        baseURL: Url
    ): ServerStatus? {
        val url = URLBuilder(baseURL).apply {
            path("api", "status")
        }.build()
        logger.info { "url: $url" }
        return try {
            client.request<ServerStatus>(url) {}
        } catch (exception: Exception) {
            logger.error(exception) { "request failed" }
            null
        }
    }

    suspend fun login(
        urlInput: String,
        userIdInput: String,
        passwordInput: String?,
        onSuccess: () -> Unit = {}
    ) {
        val baseURL = Url(urlInput)
        login(baseURL, userIdInput, passwordInput, onSuccess)
    }

    suspend fun login(
        baseURL: Url,
        userIdInput: String,
        passwordInput: String?,
        onSuccess: () -> Unit = {}
    ) {
        val status = status(baseURL)

        logger.info { "status: $status" }
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
                logger.info { "headers: $headers" }
                logger.info { "setCookie: ${setCookie()}" }
                parse(LoginResponse.serializer()) to headers["SESSION"]
            }
            PentaViz.multiplayerState.value = when (loginResponse) {
                is LoginResponse.UserIdRejected -> {
                    MultiplayerState.UserIDRejected(
                        baseUrl = baseURL,
                        userId = userIdInput,
                        reason = loginResponse.reason
                    )
                }
                is LoginResponse.IncorrectPassword -> MultiplayerState.RequiresPassword(
                    baseUrl = baseURL,
                    userId = userIdInput
                )
                is LoginResponse.Success -> MultiplayerState.Connected(
                    baseUrl = baseURL,
                    userId = userIdInput,
                    session = sessionId ?: throw IllegalStateException("missing SESSION header")
                ).also { state ->
                    onSuccess()

                    val whoAmIUrl = URLBuilder(baseURL).apply {
                        path("whoami")
                    }.build()
                    client.authenticatedRequest(whoAmIUrl, state, HttpMethod.Get) {
                        authenticateWith(state)
                    }.run {
                        logger.suspendInfo { "response: " + readText() }
                    }
                }
            }
        }
    }

    suspend fun listGames(
        state: MultiplayerState.Connected,
        gamesList: MutableObservableList<GameSessionInfo>,
        onSuccess: () -> Unit = {}
    ) {
        val listGamesUrl = URLBuilder(state.baseUrl)
            .path("api", "games")
            .build()

        val receivedList = try {
            client.authenticatedRequest(listGamesUrl, state, HttpMethod.Get, GameSessionInfo.serializer().list)
        } catch (exception: Exception) {
            logger.error(exception) { "request failed" }
            // TODO: add state: connection lost
            PentaViz.multiplayerState.value = MultiplayerState.Disconnected(
                baseUrl = state.baseUrl, userId = state.userId
            )
            return
        }

        gamesList.replace(receivedList)
        onSuccess()
    }

    suspend fun createGameAndConnect(state: MultiplayerState.Connected) {
        val createGameUrl = URLBuilder(state.baseUrl)
            .path("api", "game", "create")
            .build()
        val gameSessionInfo =
            client.authenticatedRequest(createGameUrl, state, HttpMethod.Get, GameSessionInfo.serializer())
        connectToGame(state, gameSessionInfo)
    }

    @UseExperimental(ExperimentalStdlibApi::class)
    suspend fun connectToGame(state: MultiplayerState.Connected, game: GameSessionInfo) {
        val wsUrl = URLBuilder(state.baseUrl)
            .path("ws", "game", game.id)
            .build()
        try {
            client.webSocket(
                host = wsUrl.host,
                port = wsUrl.port, path = wsUrl.fullPath,
                request = {
                    authenticateWith(state)
                    if (wsUrl.protocol == URLProtocol.HTTPS)
                        url.protocol = URLProtocol.WSS
                }
            ) {
                logger.info { "connection opened" }
                PentaViz.gameStateProperty.value = ClientGameState()
                PentaViz.updateBoard()

                outgoing.send(Frame.Text(state.session))

                logger.info { "setting multiplayerStatus to Playing" }
                val observingState = MultiplayerState.Observing(
                    baseUrl = state.baseUrl,
                    userId = state.userId,
                    session = state.session,
                    game = game,
                    websocketSession = this@webSocket,
                    running = true
                )
                PentaViz.multiplayerState.value = observingState.also {
                    logger.info { "setting multiplayerStatus to $it" }
                }

                try {
                    val notationListJson = (incoming.receive() as Frame.Text).readText()
                    logger.info { "receiving notation $notationListJson" }
                    val history = json.parse(SerialNotation.serializer().list, notationListJson)
                    withContext(Dispatchers.UI) {
                        history.forEach { notation ->
                            notation.asMove(PentaViz.gameState).also {
                                PentaViz.gameState.processMove(it)
                            }
                        }
                    }

                    send(Frame.Ping("hello".encodeToByteArray()))
                    loop@ while (true) {
                        logger.info { "awaiting frame" }
                        val frame = try {
//                                withTimeout(1000) {
                            incoming.receive()
//                                }
                            /*} catch(e: TimeoutCancellationException) {
                                logger.error { "timeout" }
                                break */
                        } catch (e: Exception) {
                            logger.error(e) { "exception onClose ${e.message}" }
                            throw e
                            // TODO transition to state `ConnectionLost`
                        } finally {

                        }
                        logger.info { "received frame: $frame" }
                        when (frame) {
//                            is Frame.Binary -> TODO("")
                            is Frame.Text -> {
                                val notationJson = frame.readText()
                                logger.info { "receiving notation $notationJson" }
                                val notation = json.parse(SerialNotation.serializer(), notationJson)
                                notation.asMove(PentaViz.gameState).also {
                                    // apply move
                                    withContext(Dispatchers.UI) {
                                        PentaViz.gameState.processMove(it)
                                    }

                                }
                            }
                        }

//                    outgoing.send(Frame.Text("received"))
                    }
                } catch (e: ClosedReceiveChannelException) {
                    val reason = closeReason.await()
                    logger.debug(e) { "onClose $reason" }
                    // TODO transition to state `ConnectionLost`
                } catch (e: Exception) {
                    logger.error(e) { "exception onClose ${e.message}" }
                    // TODO transition to state `ConnectionLost`
                } finally {
                    logger.info { "connection closing" }
                }
            }
        } catch (e: ClosedReceiveChannelException) {
            logger.debug(e) { "ws onClose ${e.message}" }
            // TODO transition to state `ConnectionLost`
        } catch (e: Throwable) {
            logger.debug(e) { "ws onClose ${e.message}" }
            // TODO transition to state `ConnectionLost`
        } finally {
            logger.info { "ws connection closing" }
        }

//        PentaViz.gameState.players.replace(listOf(PlayerState("triangle", "triangle"), PlayerState("square", "square")))
//        PentaViz.resetBoard()
        logger.info { "connection closed" }
        PentaViz.gameStateProperty.value = ClientGameState(2)
        PentaViz.updateBoard()

        // connection closed "normally" ?
        PentaViz.multiplayerState.value = MultiplayerState.Connected(
            baseUrl = state.baseUrl,
            userId = state.userId,
            session = state.session
        )
    }

    suspend fun joinGame(state: MultiplayerState.Observing) {
        val joinGameUrl = URLBuilder(state.baseUrl)
            .path("api", "game", state.game.id, "join")
            .build()

        client.authenticatedRequest(joinGameUrl, state, HttpMethod.Get)
    }

    suspend fun startGame(state: MultiplayerState.Observing) {
        val startGameUrl = URLBuilder(state.baseUrl)
            .path("api", "game", state.game.id, "start")
            .build()

        client.authenticatedRequest(startGameUrl, state, HttpMethod.Get)
    }

    override fun generate(dependency: MyViewFactory<VIEW>): VIEW = with(dependency) {
        swap(PentaViz.gameStateProperty.transform { gameState ->
            swap(
                PentaViz.multiplayerState.transform { state ->
                    when (state) {
                        is MultiplayerState.Disconnected, is MultiplayerState.UserIDRejected -> {
                            val urlInput = StandardObservableProperty(state.baseUrl.toString())
                            val userIdInput = StandardObservableProperty(state.userId)

                            vertical {
                                +space()
                                if (state is MultiplayerState.UserIDRejected) {
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
                                            GlobalScope.launch(Dispatchers.UI) {
                                                login(urlInput.value, userIdInput.value, null)
                                            }
                                        }
                                    )
                                }.setHeight(32f)
                                +space()
                            } to Animation.Fade
                        }
                        is MultiplayerState.RequiresPassword -> {
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
                                            GlobalScope.launch(Dispatchers.UI) {
                                                login(state.baseUrl, state.userId, passwordInput.value)
                                            }
                                        }
                                    )
                                }.setHeight(32f)
                                -button(
                                    label = "back",
                                    onClick = {
                                        PentaViz.multiplayerState.value = MultiplayerState.Disconnected(
                                            baseUrl = state.baseUrl,
                                            userId = state.userId
                                        )
                                    }
                                )
                                +space()

                            } to Animation.Fade
                        }
                        is MultiplayerState.Connected -> {
                            //TODO: receive games list initially
                            val games = mutableObservableListOf<GameSessionInfo>()
                            games.onListUpdate.add {
                                logger.info { "list updated: ${it.joinToString()}" }
                            }
                            GlobalScope.launch(Dispatchers.UI) {
                                listGames(state, games)
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
                                                PentaViz.multiplayerState.value = MultiplayerState.Disconnected(
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
                                                GlobalScope.launch(/*Dispatchers.UI*/) {
                                                    createGameAndConnect(state)
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
                                                            GlobalScope.launch(Dispatchers.UI) {
                                                                connectToGame(state, game)
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
                                    GlobalScope.launch(Dispatchers.UI) {
                                        listGames(state, games) {
                                            refreshing.value = false
                                        }
                                    }
                                }
                            ).setWidth(200f) to Animation.Fade
                        }
                        is MultiplayerState.Observing -> {
                            vertical {
                                -text("gameId: ${state.game.id}")
                                -text("owner: ${state.game.owner}")
                                // TODO: list of connected observers
                                // TODO: chat ?
                                -swap(
                                    CombineObservableProperty2(
                                        gameState.initializedProperty,
                                        gameState.players.onListUpdate
                                    ) { initialized, players ->
                                        if (!initialized && players.none { it.id == state.userId }) {
                                            button(
                                                label = "Join",
                                                onClick = {
                                                    GlobalScope.launch(Dispatchers.UI) {
                                                        joinGame(state)
                                                    }
                                                }
                                            )
                                        } else {
                                            // TODO: add "spectate" ?
                                            space()
                                        } to Animation.Fade
                                    }
                                )
                                +space()
                                // TODO: move to top row instead
                                -swap(gameState.initializedProperty.transform {
                                    if (!it && state.game.owner == state.userId) {
                                        button(
                                            // TODO: hide once started
                                            label = "Start",
                                            onClick = {
                                                GlobalScope.launch(Dispatchers.UI) {
                                                    startGame(state)
                                                }
                                            }
                                        )
                                    } else {
                                        space()
                                    } to Animation.Fade
                                })
                                +space()
                                -button(
                                    label = "Leave Game",
                                    onClick = {
                                        GlobalScope.launch(Dispatchers.UI) {
                                            state.leave()
                                        }
                                    }
                                )
                            } to Animation.Fade
                        }
                    }
                }
            ) to Animation.Fade
        })
    }
}
