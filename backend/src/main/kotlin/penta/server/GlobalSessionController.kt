package penta.server

import com.lightningkite.reacktive.list.WrapperObservableList
import com.lightningkite.reacktive.map.WrapperObservableMap
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
    val observeringSessions = WrapperObservableMap<UserSession, DefaultWebSocketServerSession>().apply {
        onMapRemove.add { session, new_wss ->
            GlobalScope.launch(handler) {
                values.forEach { wss ->
                    if (wss != new_wss) {
                        wss.outgoing.send(
                            Frame.Text(
                                json.stringify(GlobalEvent.serializer(), GlobalEvent.Leave(session.userId, ""))
                            )
                        )
                    }
                }
            }
        }
        onMapPut.add { session, hadPrevious, _, new_wss ->
            GlobalScope.launch(handler) {
                values.forEach { wss ->
                    if (wss != new_wss) {
                        wss.outgoing.send(
                            Frame.Text(
                                json.stringify(GlobalEvent.serializer(), GlobalEvent.Join(session.userId))
                            )
                        )
                    }
                }
            }
        }
    }
    val chatHistory = WrapperObservableList<GlobalEvent.Message>().apply {
        onListAdd.add { new_message, _ ->
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

            observeringSessions[session] = this

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
                            chatHistory += event
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
            observeringSessions.remove(session)
            val reason = closeReason.await()
            logger.debug(e) { "onClose $reason" }
        } catch (e: ClosedReceiveChannelException) {
            observeringSessions.remove(session)
            val reason = closeReason.await()
            logger.error { "onClose $reason" }
        } catch (e: ClosedSendChannelException) {
            observeringSessions.remove(session)
            val reason = closeReason.await()
            logger.error { "onClose $reason" }
        } catch (e: Exception) {
            observeringSessions.remove(session)
            logger.error(e) { "exception onClose ${e.message}" }
        } finally {

        }

    }
}