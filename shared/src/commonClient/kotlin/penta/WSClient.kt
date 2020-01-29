package penta

import client
import io.ktor.client.features.websocket.webSocket
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.response.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.http.content.TextContent
import io.ktor.http.fullPath
import io.ktor.http.setCookie
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.list
import kotlinx.serialization.serializer
import mu.KotlinLogging
import penta.network.GameEvent
import penta.network.GameSessionInfo
import penta.network.LobbyEvent
import penta.network.LoginRequest
import penta.network.LoginResponse
import penta.network.ServerStatus
import penta.util.authenticateWith
import penta.util.authenticatedRequest
import penta.util.exhaustive
import penta.util.json
import penta.util.parse

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
        dispatch: (ConnectionState) -> Unit
    ): ConnectionState {
        val baseURL = Url(urlInput)
        return login(baseURL, userIdInput, passwordInput, dispatch)
    }

    suspend fun login(
        baseURL: Url,
        userIdInput: String,
        passwordInput: String?,
        dispatch: (ConnectionState) -> Unit
    ): ConnectionState {
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
            val connectionState = when (loginResponse) {
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
                )
            }

            dispatch(connectionState)
            return connectionState
        } else {
            return ConnectionState.Unreachable(
                baseUrl = baseURL,
                userId = userIdInput
            ).also {
                dispatch(it)
            }
        }
    }

    suspend fun <T> listGames(
        state: T,
        dispatch: (ConnectionState) -> Unit
    ): List<GameSessionInfo>
        where T : ConnectionState, T : ConnectionState.HasSession {
        val listGamesUrl = URLBuilder(state.baseUrl)
            .path("api", "games")
            .build()

        val receivedList = try {
            client.authenticatedRequest(
                listGamesUrl,
                state,
                HttpMethod.Get,
                GameSessionInfo.serializer().list
            )
        } catch (exception: Exception) {
            logger.error(exception) { "request failed" }
            // TODO: add state: connection lost
            dispatch(
                ConnectionState.Disconnected(
                    baseUrl = state.baseUrl, userId = state.userId
                )
            )
            return listOf()
        }

        return receivedList
    }

    @UseExperimental(KtorExperimentalAPI::class)
    suspend fun connectToLobby(
        state: ConnectionState.Authenticated,
        dispatch: (ConnectionState) -> Unit
    ) {
        val wsUrl = URLBuilder(state.baseUrl)
            .path("ws", "lobby")
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

            val lobbyState = ConnectionState.Lobby(
                baseUrl = state.baseUrl,
                userId = state.userId,
                session = state.session,
                websocketSessionLobby = this
            )
            logger.info { "setting connectionState to $lobbyState" }
            dispatch(lobbyState)

            try {
                while (true) {
                    val notationJson = (incoming.receive() as Frame.Text).readText()
                    logger.info { "ws received: $notationJson" }

                    val _event = json.parse(LobbyEvent.serializer(), notationJson)

                    logger.info { "received event: $_event" }

                    val event = _event as? LobbyEvent.FromServer ?: run {
                        close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "event: $this cannot be sent by client"))
//                        terminate()
                        throw IllegalStateException("event: $this cannot be sent by client")
                    }

                    // TODO: dispatch LobbyEvents to redux
                    when (event) {
                        is LobbyEvent.InitialSync -> {
                            // TODO
                        }
                        is LobbyEvent.Message -> {
                            // TODO
                        }
                        is LobbyEvent.Join -> {
                            // TODO
                        }
                        is LobbyEvent.Leave -> {
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


        dispatch(
            ConnectionState.Authenticated(
                baseUrl = state.baseUrl,
                userId = state.userId,
                session = state.session
            )
        )
    }

    suspend fun createGameAndConnect(
        state: ConnectionState.Lobby,
        dispatchConnection: (ConnectionState) -> Unit,
        dispatchNotation: (GameEvent) -> Unit,
        dispatchNewBoardState: (BoardState) -> Unit
    ) {
        val createGameUrl = URLBuilder(state.baseUrl)
            .path("api", "game", "create")
            .build()
        val gameSessionInfo =
            client.authenticatedRequest(createGameUrl, state, HttpMethod.Get, GameSessionInfo.serializer())
        connectToGame(
            state = state,
            game = gameSessionInfo,
            dispatchConnection = dispatchConnection,
            dispatchNotation = dispatchNotation,
            dispatchNewBoardState = dispatchNewBoardState
        )
    }

    @UseExperimental(ExperimentalStdlibApi::class)
    suspend fun connectToGame(
        state: ConnectionState.Lobby,
        game: GameSessionInfo,
        dispatchConnection: (ConnectionState) -> Unit,
        dispatchNotation: (GameEvent) -> Unit,
        dispatchNewBoardState: (BoardState) -> Unit
    ) {
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
//            )
            logger.info { "connection opened" }
            // TODO: reset boardState ?
            dispatchNewBoardState(BoardState.create())
            // all done via redux
//            PentaViz.gameState = clientGameState
//            PentaViz.updateBoard()

            outgoing.send(Frame.Text(state.session))

            logger.info { "setting multiplayerStatus to Playing" }
            val observingState = ConnectionState.ConnectedToGame(
                baseUrl = state.baseUrl,
                userId = state.userId,
                session = state.session,
                game = game,
                websocketSessionGame = this@webSocket,
                websocketSessionLobby = state.websocketSessionLobby
            )
            logger.info { "setting connectionStatus to $observingState" }
            dispatchConnection(observingState)

            try {
                // TODO: add another observerStore
                val observersJsonList = (incoming.receive() as Frame.Text).readText()
                logger.info { "receiving observers $observersJsonList" }
                val observers = json.parse(String.serializer().list, observersJsonList)
                // TODO: dispatch action to store
                // TODO: add store/section for player list and chat history
//                penta.client.PentaViz.gameState.observersProperty.replace(observers)

                val notationListJson = (incoming.receive() as Frame.Text).readText()
                logger.info { "receiving notation $notationListJson" }
                val history = json.parse(GameEvent.serializer().list, notationListJson)
                // TODO: dispatch action to store
//                penta.client.PentaViz.gameState.isPlayback = true
//                withContext(Dispatchers.Main) {
                history.forEach { notation ->
                    dispatchNotation(notation)
                }
//                }
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
//                            is Frame.Binary -> TODO("handle binary frames")
                        is Frame.Text -> {
                            val notationJson = frame.readText()
                            logger.info { "receiving notation $notationJson" }
                            val notation = json.parse(GameEvent.serializer(), notationJson)
                            dispatchNotation(notation)
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
        logger.info { "connection closed" }

        // connection closed "normally" ?
        dispatchConnection(
            ConnectionState.Lobby(
                baseUrl = state.baseUrl,
                userId = state.userId,
                session = state.session,
                websocketSessionLobby = state.websocketSessionLobby
            )
        )

        // TODO: reset boardState
        dispatchNewBoardState(BoardState.create())
    }

    suspend fun joinGame(state: ConnectionState.ConnectedToGame) {
        val joinGameUrl = URLBuilder(state.baseUrl)
            .path("api", "game", state.game.id, "join")
            .build()

        client.authenticatedRequest(joinGameUrl, state, HttpMethod.Get)
    }

    // TODO: just send `InitGame`
    suspend fun startGame(state: ConnectionState.ConnectedToGame) {
        val startGameUrl = URLBuilder(state.baseUrl)
            .path("api", "game", state.game.id, "start")
            .build()

        client.authenticatedRequest(startGameUrl, state, HttpMethod.Get)
    }
}