package penta.server

import com.soywiz.klogger.Logger
import io.ktor.http.cio.websocket.Frame
import io.ktor.websocket.DefaultWebSocketServerSession
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.reduxkotlin.applyMiddleware
import org.reduxkotlin.createStore
import penta.LobbyState
import penta.network.LobbyEvent
import penta.util.handler
import penta.util.json
import penta.util.loggingMiddleware

data class GlobalState(
    val games: List<ServerGamestate> = listOf(),

    // lobby
    val observingSessions: Map<UserSession, DefaultWebSocketServerSession> = mapOf(),
    val lobbyState: LobbyState = LobbyState()
) {
    fun reduce(action: Any): GlobalState = when (action) {
        is GlobalAction -> when(action) {
            is GlobalAction.AddSession -> {
                GlobalScope.launch(handler) {
                    observingSessions.forEach { (session, wss) ->
                        if (wss != action.websocketSession) {
                            wss.outgoing.send(
                                Frame.Text(
                                    json.stringify(
                                        LobbyEvent.serializer(),
                                        LobbyEvent.Join(action.session.userId)
                                    )
                                )
                            )
                        }
                    }
                }
                copy(
                    observingSessions = observingSessions + (action.session to action.websocketSession)
                )
            }
            is GlobalAction.RemoveSession -> {
                val toRemove = observingSessions[action.session]
                GlobalScope.launch(handler) {
                    observingSessions.forEach { (session, wss) ->
                        if (wss != toRemove) {
                            wss.outgoing.send(
                                Frame.Text(
                                    json.stringify(LobbyEvent.serializer(), LobbyEvent.Leave(action.session.userId, ""))
                                )
                            )
                        }
                    }
                }
                copy(
                    observingSessions = observingSessions - action.session
                )
            }
            is GlobalAction.AddGame -> {
                copy(games = games + action.game)
                    .reduce(LobbyEvent.UpdateGame(action.game.info))
            }
        }
        is LobbyEvent -> {
            GlobalScope.launch(handler) {
                observingSessions.forEach { (session, ws) ->
                    ws.send(
                        Frame.Text(json.stringify(LobbyEvent.serializer(), action))
                    )
                }
            }

            copy(
                lobbyState = lobbyState.reduce(action)
            )
        }
        else -> this
    }

    sealed class GlobalAction {
        data class AddSession(
            val session: UserSession,
            val websocketSession: DefaultWebSocketServerSession
        ): GlobalAction()
        data class RemoveSession(
            val session: UserSession
        ): GlobalAction()
        data class AddGame(
            val game: ServerGamestate
        ): GlobalAction()
    }

    companion object {
        private val logger = Logger(this::class.simpleName!!)
        val store = createStore(
            GlobalState::reduce,
            GlobalState(),
            applyMiddleware(loggingMiddleware(GlobalState.logger))
        )
    }
}
