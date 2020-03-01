package penta.server

import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.websocket.DefaultWebSocketServerSession
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ClosedSendChannelException
import mu.KotlinLogging
import penta.network.LobbyEvent
import penta.util.exhaustive
import penta.util.json
import java.io.IOException

object LobbyHandler {
    private val logger = KotlinLogging.logger {}
    private val observingSessions: MutableMap<UserSession, DefaultWebSocketServerSession> = mutableMapOf()

    private val serializer = LobbyEvent.serializer()
    suspend fun handle(websocketSession: DefaultWebSocketServerSession, session: UserSession) = with(websocketSession) {
        try {
            // play back history
            logger.info { "sending observers" }
            logger.info { "sending chat history" }
            logger.info { "sending game list" }
            val state = GlobalState.getState()
            outgoing.send(
                Frame.Text(
                    json.stringify(
                        serializer,
                        LobbyEvent.InitialSync(
                            users = state.observingSessions.keys.map { it.userId },
                            chat = state.lobbyState.chat.take(50),
                            games = GameController.listActiveGames()
                                .associate { gameState ->
                                    gameState.serverGameId to gameState.info
                                }
                        )
                    )
                )
            )

            // register as observer

//            observeringSessions[session] = this

            // TODO: also send initial sync through action ?
            GlobalState.dispatch(GlobalState.GlobalAction.AddSession(session, this))

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
                            GlobalState.dispatch(event)
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
            GlobalState.dispatch(GlobalState.GlobalAction.RemoveSession(session))
            val reason = closeReason.await()
            logger.debug { "onClose ${session.userId} $reason ${e.message}" }
        } catch (e: ClosedReceiveChannelException) {
            GlobalState.dispatch(GlobalState.GlobalAction.RemoveSession(session))
            val reason = closeReason.await()
            logger.error { "onClose ${session.userId} $reason" }
        } catch (e: ClosedSendChannelException) {
            GlobalState.dispatch(GlobalState.GlobalAction.RemoveSession(session))
            val reason = closeReason.await()
            logger.error { "onClose ${session.userId} $reason" }
        } catch (e: Exception) {
            GlobalState.dispatch(GlobalState.GlobalAction.RemoveSession(session))
            logger.error(e) { e.localizedMessage }
            logger.error { "exception onClose ${session.userId} ${e.message}" }
        } finally {

        }

    }
}

