package penta.server

import com.soywiz.klogger.Logger
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.websocket.DefaultWebSocketServerSession
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import org.reduxkotlin.Store
import org.reduxkotlin.applyMiddleware
import org.reduxkotlin.createStore
import penta.LobbyState
import penta.network.LobbyEvent
import penta.util.exhaustive
import penta.util.handler
import penta.util.json
import penta.util.loggingMiddleware

object LobbyHandler {
    private val logger = Logger(this::class.simpleName!!)
    private val observingSessions: MutableMap<UserSession, DefaultWebSocketServerSession> = mutableMapOf()

    // TODO use this store
    val store: Store<LobbyState> = createStore(
        ::reducer,
        LobbyState(),
        applyMiddleware(loggingMiddleware(logger))
    )

    fun reducer(lobbyState: LobbyState = LobbyState(), action: Any): LobbyState {
        if(action !is LobbyEvent) {
            return lobbyState
        }

        logger.info { "processing: $action" }

        GlobalScope.launch(handler) {
            observingSessions.forEach { (session, ws) ->
                ws.send(
                    Frame.Text(json.stringify(LobbyEvent.serializer(), action))
                )
            }
        }

        return lobbyState.reduce(action)
    }

    // move into store
    fun observeringSessionsPut(
        session: UserSession,
        defaultWebSocketServerSession: DefaultWebSocketServerSession
    ) {
        GlobalScope.launch(handler) {
            observingSessions.values.forEach { wss ->
                if (wss != defaultWebSocketServerSession) {
                    wss.outgoing.send(
                        Frame.Text(
                            json.stringify(LobbyEvent.serializer(), LobbyEvent.Join(session.userId))
                        )
                    )
                }
            }
        }
    }
    fun observeringSessionsRemove(
        session: UserSession
    ) {
        val removedSession = observingSessions.remove(session)
        GlobalScope.launch(handler) {
            observingSessions.values.forEach { wss ->
                if (wss != removedSession) {
                    wss.outgoing.send(
                        Frame.Text(
                            json.stringify(LobbyEvent.serializer(), LobbyEvent.Leave(session.userId, ""))
                        )
                    )
                }
            }
        }
    }

//    private val mutableChatHistory = mutableListOf<LobbyEvent.Message>()
//    val chatHistory: List<LobbyEvent.Message> = mutableChatHistory
//    fun chatHistoryAdd(new_message: LobbyEvent.Message) {
//        mutableChatHistory += new_message
//        GlobalScope.launch(handler) {
//            observingSessions.values.forEach { wss ->
//                wss.outgoing.send(
//                    Frame.Text(
//                        json.stringify(LobbyEvent.serializer(), new_message)
//                    )
//                )
//            }
//        }
//    }

    private val serializer = LobbyEvent.serializer()
    suspend fun handle(websocketSession: DefaultWebSocketServerSession, session: UserSession) = with(websocketSession) {
        try {
            // play back history
            logger.info { "sending observers" }
            logger.info { "sending chat history" }
            logger.info { "sending game list" }
            outgoing.send(
                Frame.Text(
                    json.stringify(
                        serializer,
                        LobbyEvent.InitialSync(
                            users = observingSessions.keys.map { it.userId },
                            chat = store.state.chat.take(50),
                            games = GameController.games.associate { gameState ->
                                gameState.id to gameState.info
                            }
                        )
                    )
                )
            )

            // register as observer

//            observeringSessions[session] = this
            observeringSessionsPut(session, this)

            while (true) {
                val notationJson = (incoming.receive() as Frame.Text).readText()
                logger.info { "ws received: $notationJson" }

                val event = json.parse(serializer, notationJson) as? LobbyEvent.FromClient ?: run {
                    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "event: $this cannot be sent by client"))
                    return
                }
                when (event) {
                    is LobbyEvent.Message -> {
                        if (session.userId == event.userId) {
                            store.dispatch(event)
//                            chatHistoryAdd(event)
                        } else {
                            logger.error { "user ${session.userId} sent message from ${event.userId}" }
                        }
                    }
                    else -> {
                        TODO("unhandled event: $event")
                    }
                }.exhaustive
            }
        } catch (e: IOException) {
            observeringSessionsRemove(session)
            val reason = closeReason.await()
            logger.debug { e }
            logger.debug { "onClose $reason" }
        } catch (e: ClosedReceiveChannelException) {
            observeringSessionsRemove(session)
            val reason = closeReason.await()
            logger.error { "onClose $reason" }
        } catch (e: ClosedSendChannelException) {
            observeringSessionsRemove(session)
            val reason = closeReason.await()
            logger.error { "onClose $reason" }
        } catch (e: Exception) {
            observeringSessionsRemove(session)
            logger.error { e }
            logger.error { "exception onClose ${e.message}" }
        } finally {

        }

    }
}

