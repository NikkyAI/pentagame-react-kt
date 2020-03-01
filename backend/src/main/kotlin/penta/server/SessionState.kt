package penta.server

import SessionEvent
import com.soywiz.klogger.Logger
import io.ktor.websocket.DefaultWebSocketServerSession
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.reduxkotlin.Reducer
import penta.BoardState
import penta.PlayerIds
import penta.network.GameEvent
import penta.util.exhaustive
import penta.util.handler

data class SessionState(
    val boardState: BoardState = BoardState.create(),
    val observingSessions: Map<UserSession, DefaultWebSocketServerSession> = mapOf(),
    val playingUsers: Map<PlayerIds, ServerUserInfo> = mapOf()
) {
    companion object {
        private val logger = Logger(this::class.simpleName!!)
        sealed class Actions {
            data class AddObserver(
                val session: UserSession,
                val wss: DefaultWebSocketServerSession
            ) : Actions()

            data class RemoveObserver(
                val session: UserSession
            ) : Actions()
        }

        val reducer: Reducer<SessionState> = { state, action ->
            // TODO: modify state
            when(action) {
                is org.reduxkotlin.ActionTypes.INIT -> {
                    logger.info { "received INIT" }
                    state
                }
                is org.reduxkotlin.ActionTypes.REPLACE -> {
                    logger.info { "received REPLACE" }
                    state
                }
                is BoardState.RemoveIllegalMove -> {
                    state.copy(
                        boardState = state.boardState.reduce(action)
                    )
                }
                is GameEvent -> {
                    state.copy(
                        boardState = state.boardState.reduce(action)
                    )
                }
                is AuthedSessionEvent -> {
                    val user = action.user
                    when(val action = action.event) {
                        is SessionEvent.WrappedGameEvent -> {
                            state.copy(
                                boardState = state.boardState.reduce(action.event)
                            )
                        }
                        is SessionEvent.PlayerJoin -> {
                            // TODO: validate origin of event
                            logger.info { "TODO: check $user" }
                            state.copy(
                                playingUsers = state.playingUsers + (action.player to ServerUserInfo(user, action.user.figureId))
                            )
                        }
                        is SessionEvent.PlayerLeave -> TODO()
                        is SessionEvent.IllegalMove -> TODO()
                        is SessionEvent.Undo -> TODO()
                    }
                }
                is Actions -> {
                    when (action) {
                        is Actions.AddObserver -> {
                            GlobalScope.launch(handler) {
                                state.observingSessions.forEach { (session, wss) ->
                                    if (wss != action.wss) {
                                        // TODO: wrap Move into other object
                                        // TODO: put observer join/leave
//                                     wss.outgoing.send(
//                                         Frame.Text(
//                                             json.stringify(GameEvent.serializer(), GameEvent.ObserverLeave(session.userId))
//                                         )
//                                     )
                                    }
                                }
                            }

                            state.copy(observingSessions = state.observingSessions + (action.session to action.wss))
                        }
                        is Actions.RemoveObserver -> {
                            GlobalScope.launch(handler) {
                                state.observingSessions.forEach { (session, wss) ->
                                    if (wss != state.observingSessions[action.session]) {
                                        // TODO: wrap Move into other object
                                        // TODO: put observer join/leave
//                                     wss.outgoing.send(
//                                         Frame.Text(
//                                             json.stringify(GameEvent.serializer(), GameEvent.ObserverJoin(session.userId))
//                                         )
//                                     )
                                    }
                                }
                            }

                            state.copy(observingSessions = state.observingSessions - action.session)
                        }
                    }.exhaustive
                }
                else -> {
                    error("$action is of unhandled type")
                }
            }

        }
    }
}

