package penta.server

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
import mu.KotlinLogging
import penta.network.GlobalEvent
import penta.util.exhaustive
import penta.util.handler
import penta.util.json

object GlobalSessionController {
    private val logger = KotlinLogging.logger {}
    private val observeringSessions = mutableMapOf<UserSession, DefaultWebSocketServerSession>()
    fun observeringSessionsPut(
        session: UserSession,
        defaultWebSocketServerSession: DefaultWebSocketServerSession
    ) {
        GlobalScope.launch(handler) {
            observeringSessions.values.forEach { wss ->
                if (wss != defaultWebSocketServerSession) {
                    wss.outgoing.send(
                        Frame.Text(
                            json.stringify(GlobalEvent.serializer(), GlobalEvent.Join(session.userId))
                        )
                    )
                }
            }
        }
    }
    fun observeringSessionsRemove(
        session: UserSession
    ) {
        val removedSession = observeringSessions.remove(session)
        GlobalScope.launch(handler) {
            observeringSessions.values.forEach { wss ->
                if (wss != removedSession) {
                    wss.outgoing.send(
                        Frame.Text(
                            json.stringify(GlobalEvent.serializer(), GlobalEvent.Leave(session.userId, ""))
                        )
                    )
                }
            }
        }
    }

    private val mutableChatHistory = mutableListOf<GlobalEvent.Message>()
    val chatHistory: List<GlobalEvent.Message> = mutableChatHistory
    fun chatHistoryAdd(new_message: GlobalEvent.Message) {
        mutableChatHistory += new_message
        GlobalScope.launch(handler) {
            observeringSessions.values.forEach { wss ->
                wss.outgoing.send(
                    Frame.Text(
                        json.stringify(GlobalEvent.serializer(), new_message)
                    )
                )
            }
        }
    }

    private val serializer = GlobalEvent.serializer()
    suspend fun handle(websocketSession: DefaultWebSocketServerSession, session: UserSession) = with(websocketSession) {
        try {
            // play back history
            logger.info { "sending observers" }
            logger.info { "sending chat history" }
            outgoing.send(
                Frame.Text(
                    json.stringify(
                        serializer,
                        GlobalEvent.InitialSync(
                            users = observeringSessions.keys.map { it.userId },
                            chat = chatHistory.take(50)
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

                val event = json.parse(serializer, notationJson) as? GlobalEvent.FromClient ?: run {
                    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "event: $this cannot be sent by client"))
                    return
                }
                when (event) {
                    is GlobalEvent.Message -> {
                        if (session.userId == event.userId) {
                            chatHistoryAdd(event)
                        } else {
                            logger.error("user ${session.userId} sent message from ${event.userId}")
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
            logger.debug(e) { "onClose $reason" }
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
            logger.error(e) { "exception onClose ${e.message}" }
        } finally {

        }

    }
}