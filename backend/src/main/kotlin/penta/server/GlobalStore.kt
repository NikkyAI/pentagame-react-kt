package penta.server

import com.soywiz.klogger.Logger
import io.ktor.http.cio.websocket.Frame
import io.ktor.websocket.DefaultWebSocketServerSession
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.list
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.reduxkotlin.applyMiddleware
import org.reduxkotlin.createStore
import penta.LobbyState
import penta.network.GameEvent
import penta.network.LobbyEvent
import penta.server.db.Game
import penta.util.handler
import penta.util.json
import penta.util.loggingMiddleware

data class GlobalState(
    val games: List<ServerGamestate> = loadGames(),

    // lobby
    val observingSessions: Map<UserSession, DefaultWebSocketServerSession> = mapOf(),
    val lobbyState: LobbyState = LobbyState()
) {
    fun reduce(action: Any): GlobalState = when (action) {
        is GlobalAction -> when (action) {
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
                val game = transaction {
                    addLogger(Slf4jSqlDebugLogger)
                    Game.new {
                        gameId = action.game.serverGameId
                        history = json.stringify(
                            GameEvent.serializer().list,
                            action.game.boardStateStore.state.history.map { it.toSerializable() }
                        )
                        owner = UserManager.toDBUser(action.game.owner)
                    }
                }
                transaction {
                    addLogger(Slf4jSqlDebugLogger)
                    game.players = SizedCollection(
                        action.game.boardStateStore.state.players.mapNotNull {
                            UserManager.findDBUser(it.id)
                        }
                    )
                }
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
        ) : GlobalAction()

        data class RemoveSession(
            val session: UserSession
        ) : GlobalAction()

        data class AddGame(
            val game: ServerGamestate
        ) : GlobalAction()
    }

    companion object {
        private val logger = Logger(this::class.simpleName!!)
        val store = createStore(
            GlobalState::reduce,
            GlobalState(),
            applyMiddleware(loggingMiddleware(GlobalState.logger))
        )

        fun loadGames(): List<ServerGamestate> {
            return transaction {
                Game.all().map {
                    GameController.idCounter++
                    ServerGamestate(
                        it.gameId,
                        it.owner.let { u ->
                            UserManager.convert(u)
                        }
                    ).apply {
                        // apply all history
                        json.parse(GameEvent.serializer().list, it.history).forEach {
                            boardStateStore.dispatch(it)
                        }
                    }
                }
            }
        }
    }
}
