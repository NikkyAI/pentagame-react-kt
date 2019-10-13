package penta.server

import com.lightningkite.reacktive.list.MutableObservableList
import com.lightningkite.reacktive.list.mutableObservableListOf
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.websocket.DefaultWebSocketServerSession
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import mu.KotlinLogging
import penta.BoardState
import penta.PentaMove
import penta.PlayerState
import penta.SerialNotation
import penta.json
import penta.network.GameSessionInfo
import penta.util.suspendDebug
import penta.util.suspendError

/***
 * represents a pentagame match
 */
class ServerGamestate(
    val id: String,
    var ownerId: String
): BoardState() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    init {
        // TODO: init game later
        logger.info { "players: ${players.joinToString()}"}
        players.addAll(listOf(PlayerState(ownerId, "triangle"), PlayerState("other", "square")))
        players.removeAt(0)
        logger.info { "initializing with ${players.joinToString()}" }
        currentPlayerProperty.value = players.first()
        processMove(PentaMove.InitGame(players.toList()))
    }
    var running: Boolean = false
    val observers = mutableMapOf<UserSession, DefaultWebSocketServerSession>()

    val info: GameSessionInfo get() {
        return GameSessionInfo(
            id = id,
            running = running,
            turn = turn,
            players = players.map { it.id },
            observers = observers.keys.map { it.userId }
        )
    }

    val serializer = SerialNotation.serializer()

    suspend fun handle(websocketSession: DefaultWebSocketServerSession, session: UserSession) = with(websocketSession) {
        observers[session] = this
        try {
//            val channel = Channel<String>()
//            launch {
//                channel.consumeEach {
//
//                }
//            }

            // play back history
            history.forEach {
                logger.info { "transmitting ${history.joinToString()}" }
                it.toSerializableList().forEach { serialNotation ->
                    outgoing.send(Frame.Text(json.stringify(serializer, serialNotation)))
                }
            }

            // new move added
            history.onListAdd.add { move, index ->
                // send
                GlobalScope.launch {
                    move.toSerializableList().forEach { serialNotation ->
                        outgoing.send(Frame.Text(json.stringify(serializer, serialNotation)))
                    }
                }
            }
            while(true) {
                val notationJson = (incoming.receive() as Frame.Text).readText()
                logger.info { "ws received: $notationJson" }

                val notation = json.parse(serializer, notationJson)
                val moves = SerialNotation.toMoves(
                    listOf(notation),
                    this@ServerGamestate
                ) {
                  this@ServerGamestate.processMove(it)
                }
                // apply move ?
            }
        } catch (e: ClosedReceiveChannelException) {
            observers.remove(session)
            logger.suspendDebug(e) { "onClose ${closeReason.await()}" }
        } catch (e: Throwable) {
            observers.remove(session)
            logger.suspendError(e) { "onClose ${closeReason.await()}" }
        }

    }

}
