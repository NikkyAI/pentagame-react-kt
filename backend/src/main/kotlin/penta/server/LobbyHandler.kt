package penta.server

import com.soywiz.klogger.Logger
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.websocket.DefaultWebSocketServerSession
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.io.IOException
import penta.network.LobbyEvent
import penta.server.GlobalState.Companion.store
import penta.util.exhaustive
import penta.util.json

object LobbyHandler {
    private val logger = Logger(this::class.simpleName!!)
    private val observingSessions: MutableMap<UserSession, DefaultWebSocketServerSession> = mutableMapOf()

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
                            chat = store.state.lobbyState.chat.take(50),
                            games = store.state.games.associate { gameState ->
                                gameState.serverGameId to gameState.info
                            }
                        )
                    )
                )
            )

            // register as observer

//            observeringSessions[session] = this

            // TODO: also send initial sync through action ?
            store.dispatch(GlobalState.GlobalAction.AddSession(session, this))

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
            store.dispatch(GlobalState.GlobalAction.RemoveSession(session))
            val reason = closeReason.await()
            logger.debug { "onClose ${session.userId} $reason ${e.message}" }
        } catch (e: ClosedReceiveChannelException) {
            store.dispatch(GlobalState.GlobalAction.RemoveSession(session))
            val reason = closeReason.await()
            logger.error { "onClose ${session.userId} $reason" }
        } catch (e: ClosedSendChannelException) {
            store.dispatch(GlobalState.GlobalAction.RemoveSession(session))
            val reason = closeReason.await()
            logger.error { "onClose ${session.userId} $reason" }
        } catch (e: Exception) {
            store.dispatch(GlobalState.GlobalAction.RemoveSession(session))
            logger.error { e }
            logger.error { "exception onClose ${session.userId} ${e.message}" }
        } finally {

        }

    }
}

