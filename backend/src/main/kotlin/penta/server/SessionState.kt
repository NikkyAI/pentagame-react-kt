package penta.server

import io.ktor.websocket.DefaultWebSocketServerSession
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.reduxkotlin.Reducer
import penta.util.exhaustive
import penta.util.handler

data class SessionState(
    val observingSessions: Map<UserSession, DefaultWebSocketServerSession> = mapOf()
) {
    companion object {
        private val logger = KotlinLogging.logger {}
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