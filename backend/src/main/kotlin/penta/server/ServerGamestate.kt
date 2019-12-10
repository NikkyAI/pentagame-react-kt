package penta.server

import com.lightningkite.reacktive.map.WrapperObservableMap
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.websocket.DefaultWebSocketServerSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import kotlinx.serialization.list
import kotlinx.serialization.serializer
import mu.KotlinLogging
import penta.BoardState
import penta.PentaMove
import penta.PlayerState
import penta.network.GameEvent
import penta.util.json
import penta.network.GameSessionInfo
import penta.util.handler

/***
 * represents a pentagame match
 */
class ServerGamestate(
    val id: String,
    var owner: User
) : BoardState() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    var running: Boolean = false
    val observeringSessions = WrapperObservableMap<UserSession, DefaultWebSocketServerSession>().apply {
        onMapRemove.add { session, new_wss ->
            GlobalScope.launch(handler) {
                values.forEach { wss ->
                    if(wss != new_wss) {
                        wss.outgoing.send(Frame.Text(
                            json.stringify(GameEvent.serializer(), GameEvent.ObserverLeave(session.userId))
                        ))
                    }
                }
            }
        }
        onMapPut.add { session, hadPrevious,_, new_wss ->
            GlobalScope.launch(handler) {
                values.forEach { wss ->
                    if(wss != new_wss) {
                        wss.outgoing.send(
                            Frame.Text(
                                json.stringify(GameEvent.serializer(), GameEvent.ObserverJoin(session.userId))
                            )
                        )
                    }
                }
            }
        }
    }

    val info: GameSessionInfo
        get() {
            return GameSessionInfo(
                id = id,
                owner = owner.userId,
                running = running,
                turn = turn,
                players = players.map { it.id },
                observers = observeringSessions.keys.map { it.userId }
            )
        }

    private val serializer = GameEvent.serializer()

    suspend fun handle(websocketSession: DefaultWebSocketServerSession, session: UserSession) = with(websocketSession) {
        try {
            observeringSessions[session] = this
            // play back history
            logger.info { "sending observers" }
            outgoing.send(Frame.Text(json.stringify(String.serializer().list, observeringSessions.keys.map { it.userId })))


            logger.info { "play back history" }
            outgoing.send(Frame.Text(json.stringify(serializer.list, history.map { it.toSerializable() })))
//            history.forEach { move ->
//                logger.info { "transmitting move $move" }
//                outgoing.send(Frame.Text(json.stringify(serializer, move.toSerializable())))
//            }

            // new move added
            history.onListAdd.add { move, index ->
                logger.info { "transmitting move $move" }
                // send
                GlobalScope.launch(handler) {
                    outgoing.send(Frame.Text(json.stringify(serializer, move.toSerializable())))
                }
            }
            while (true) {
                val notationJson = (incoming.receive() as Frame.Text).readText()
                logger.info { "ws received: $notationJson" }

                val notation = json.parse(serializer, notationJson)
                notation.asMove(this@ServerGamestate).also {
                    this@ServerGamestate.processMove(it) { illegalMove ->
                        logger.error {
                            "handle illegal move: $illegalMove"
                        }
                        GlobalScope.launch(Dispatchers.Default + handler) {
                            outgoing.send(
                                Frame.Text(
                                    json.stringify(
                                        GameEvent.serializer(),
                                        illegalMove.toSerializable()
                                    )
                                )
                            )
                        }
                    }
                }
                // apply move ?
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

    fun requestJoin(user: User) {
        if (players.size > 3) {
            logger.error { "max player limit reached" }
            return
        }
        if (gameStarted) {
            logger.error { "game has already started" }
            return
        }

        val shapes = listOf("triangle", "square", "cross", "circle")
        processMove(PentaMove.PlayerJoin(PlayerState(user.userId, shapes[players.size])))
    }

    fun requestStart(user: User) {
        if (user.userId == owner.userId) {
            processMove(PentaMove.InitGame)
        }
    }
}
