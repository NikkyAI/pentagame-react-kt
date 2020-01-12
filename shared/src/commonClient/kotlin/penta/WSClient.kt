package penta

import client
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
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.content.TextContent
import io.ktor.http.fullPath
import io.ktor.http.setCookie
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.withContext
import kotlinx.serialization.list
import kotlinx.serialization.serializer
import mu.KotlinLogging
import penta.network.GameSessionInfo
import penta.network.GlobalEvent
import penta.network.LoginRequest
import penta.network.LoginResponse
import penta.network.ServerStatus
import penta.util.authenticateWith
import penta.util.authenticatedRequest
import penta.util.exhaustive
import penta.util.json
import penta.util.parse
import penta.util.suspendInfo
import io.ktor.http.cio.websocket.close
import penta.client.PentaViz
import penta.network.GameEvent

@Deprecated("move code away")
object WSClient {
    private val logger = KotlinLogging.logger {}

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
            PentaViz.multiplayerState = when (loginResponse) {
                is LoginResponse.UserIdRejected -> {
                    ConnectionState.UserIDRejected(
                        baseUrl = baseURL,
                        userId = userIdInput,
                        reason = loginResponse.reason
                    )
                }
                is LoginResponse.IncorrectPassword -> ConnectionState.RequiresPassword(
                    baseUrl = baseURL,
                    userId = userIdInput
                )
                is LoginResponse.Success -> ConnectionState.Authenticated(
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
        state: ConnectionState.Lobby,
        gamesList: MutableList<GameSessionInfo>,
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
            PentaViz.multiplayerState = ConnectionState.Disconnected(
                baseUrl = state.baseUrl, userId = state.userId
            )
            return
        }

        gamesList.clear()
        gamesList.addAll(receivedList)
        onSuccess()
    }

    @UseExperimental(KtorExperimentalAPI::class)
    suspend fun connectToLobby(state: ConnectionState.Authenticated) {
        val wsUrl = URLBuilder(state.baseUrl)
            .path("ws", "global")
            .build()

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

            outgoing.send(Frame.Text(state.session))

            val observingState = ConnectionState.Lobby(
                baseUrl = state.baseUrl,
                userId = state.userId,
                session = state.session
            )
            PentaViz.multiplayerState = observingState.also {
                logger.info { "setting multiplayerStatus to $it" }
            }

            try {
                while (true) {
                    val notationJson = (incoming.receive() as Frame.Text).readText()
                    logger.info { "ws received: $notationJson" }

                    val event = json.parse(GlobalEvent.serializer(), notationJson) as? GlobalEvent.FromServer ?: run {
                        close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "event: $this cannot be sent by client"))
//                        terminate()
                        throw IllegalStateException("event: $this cannot be sent by client")
                    }

                    when(event) {
                        is GlobalEvent.InitialSync -> {
                            // TODO
                        }
                        is GlobalEvent.Message -> {
                            // TODO
                        }
                        is GlobalEvent.Join -> {
                            // TODO
                        }
                        is GlobalEvent.Leave -> {
                            // TODO
                        }
                        else -> {
                            logger.error { "unhandled event: $event" }
                            throw IllegalStateException("unhandled event: $event")
                        }
                    }.exhaustive


                }
            } catch (e: ClosedReceiveChannelException) {
                val reason = closeReason.await()
                logger.debug(e) { "onClose $reason" }
                // TODO transition to state `ConnectionLost`
            } catch (e: Exception) {
                logger.error(e) { "exception onClose ${e::class.simpleName} ${e.message}" }
                // TODO transition to state `ConnectionLost`
            } finally {
                logger.info { "connection closing" }
            }
        }


        PentaViz.multiplayerState = ConnectionState.Authenticated(
            baseUrl = state.baseUrl,
            userId = state.userId,
            session = state.session
        )
    }

    suspend fun createGameAndConnect(clientGameState: ClientGameState, state: ConnectionState.Lobby) {
        val createGameUrl = URLBuilder(state.baseUrl)
            .path("api", "game", "create")
            .build()
        val gameSessionInfo =
            client.authenticatedRequest(createGameUrl, state, HttpMethod.Get, GameSessionInfo.serializer())
        connectToGame(clientGameState, state, gameSessionInfo)
    }

    @UseExperimental(ExperimentalStdlibApi::class)
    suspend fun connectToGame(clientGameState: ClientGameState, state: ConnectionState.Lobby, game: GameSessionInfo) {
        val wsUrl = URLBuilder(state.baseUrl)
            .path("ws", "game", game.id)
            .build()
//        try {
        client.webSocket(
            host = wsUrl.host,
            port = wsUrl.port, path = wsUrl.fullPath,
            request = {
                authenticateWith(state)
                if (wsUrl.protocol == URLProtocol.HTTPS)
                    url.protocol = URLProtocol.WSS
            }
        ) {
//            val observerClientStore = createStore(
//                MultiplayerState.reducer,
//                MultiplayerState(),
//
//            )
            logger.info { "connection opened" }
            PentaViz.gameState = clientGameState
            PentaViz.updateBoard()

            outgoing.send(Frame.Text(state.session))

            logger.info { "setting multiplayerStatus to Playing" }
            val observingState = ConnectionState.Observing(
                baseUrl = state.baseUrl,
                userId = state.userId,
                session = state.session,
                game = game,
                websocketSession = this@webSocket,
                running = true
            )
            PentaViz.multiplayerState = observingState.also {
                logger.info { "setting multiplayerStatus to $it" }
            }

            try {
                // TODO: add another observerStore
                val observersJsonList = (incoming.receive() as Frame.Text).readText()
                logger.info { "receiving observers $observersJsonList" }
                val observers = json.parse(String.serializer().list, observersJsonList)
                // TODO: dispatch action to store
//                penta.client.PentaViz.gameState.observersProperty.replace(observers)

                val notationListJson = (incoming.receive() as Frame.Text).readText()
                logger.info { "receiving notation $notationListJson" }
                val history = json.parse(GameEvent.serializer().list, notationListJson)
                // TODO: dispatch action to store
//                penta.client.PentaViz.gameState.isPlayback = true
                withContext(Dispatchers.Main) {
                    history.forEach { notation ->
                        notation.asMove(PentaViz.gameState.boardState).also {
                            // TODO: dispatch action to store
//                            penta.client.PentaViz.gameState.processMove(it)
                        }
                    }
                }
                // TODO: dispatch action to store
//                penta.client.PentaViz.gameState.isPlayback = false

                loop@ while (true) {
                    logger.info { "awaiting frame" }
                    val frame = try {
                        incoming.receive()
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
                            val notation = json.parse(GameEvent.serializer(), notationJson)
                            notation.asMove(PentaViz.gameState.boardState).also {
                                // apply move
                                withContext(Dispatchers.Main) {
                                    // TODO: dispatch action to store
//                                    penta.client.PentaViz.gameState.processMove(it)
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
                logger.error(e) { "exception onClose ${e::class.simpleName} ${e.message}" }
                // TODO transition to state `ConnectionLost`
            } finally {
                logger.info { "connection closing" }
            }
        }
//        } catch (e: ClosedReceiveChannelException) {
//            logger.debug(e) { "ws onClose ${e.message}" }
//            // TODO transition to state `ConnectionLost`
//        } catch (e: Throwable) {
//            logger.debug(e) { "ws onClose ${e.message}" }
//            // TODO transition to state `ConnectionLost`
//        } finally {
//            logger.info { "ws connection closing" }
//        }

//        penta.client.PentaViz.gameState.players.replace(listOf(PlayerState("triangle", "triangle"), PlayerState("square", "square")))
//        penta.client.PentaViz.resetBoard()
        logger.info { "connection closed" }
        PentaViz.gameState = TODO("create new clientgamestate") //ClientGameState(2)
        PentaViz.updateBoard()

        // connection closed "normally" ?
        PentaViz.multiplayerState.value = ConnectionState.Lobby(
            baseUrl = state.baseUrl,
            userId = state.userId,
            session = state.session
        )
    }

    suspend fun joinGame(state: ConnectionState.Observing) {
        val joinGameUrl = URLBuilder(state.baseUrl)
            .path("api", "game", state.game.id, "join")
            .build()

        client.authenticatedRequest(joinGameUrl, state, HttpMethod.Get)
    }

    suspend fun startGame(state: ConnectionState.Observing) {
        val startGameUrl = URLBuilder(state.baseUrl)
            .path("api", "game", state.game.id, "start")
            .build()

        client.authenticatedRequest(startGameUrl, state, HttpMethod.Get)
    }
}