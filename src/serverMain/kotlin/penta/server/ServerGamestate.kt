package penta.server

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.websocket.DefaultWebSocketServerSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import kotlinx.serialization.list
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
    var owner: User
): BoardState() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    init {
        // TODO: init game later
//        logger.info { "players: ${players.joinToString()}"}
//        players.addAll(listOf(PlayerState(ownerId, "triangle"), PlayerState("other", "square")))
//        players.removeAt(0)
//        logger.info { "initializing with ${players.joinToString()}" }
//        currentPlayerProperty.value = players.first()
//        processMove(PentaMove.InitGame(players.toList()))
//        logger.info { "after init: " + figures.joinToString { it.id } }
    }
    var running: Boolean = false
    val observers = mutableMapOf<UserSession, DefaultWebSocketServerSession>()

    val info: GameSessionInfo get() {
        return GameSessionInfo(
            id = id,
            owner = owner.userId,
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

            // play back history
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
                GlobalScope.launch {
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
                        GlobalScope.launch(Dispatchers.Default) {
                            outgoing.send(Frame.Text(json.stringify(SerialNotation.serializer(), illegalMove.toSerializable())))
                        }
                    }
                }
                // apply move ?
            }
        } catch (e: IOException) {
            observers.remove(session)
            logger.suspendDebug(e) { "onClose ${closeReason.await()}" }
        } catch (e: ClosedReceiveChannelException) {
            observers.remove(session)
            logger.suspendDebug(e) { "onClose ${closeReason.await()}" }
        } catch (e: Throwable) {
            observers.remove(session)
            logger.suspendError(e) { "onClose ${closeReason.await()}" }
        }

    }

    fun requestJoin(user: User) {
        if(players.size > 3) {
            logger.error { "max player limit reached" }
            return
        }
        if(initialized) {
            logger.error { "game has already started" }
            return
        }

        val shapes = listOf("triangle", "square", "cross", "circle")
        processMove(PentaMove.PlayerJoin(PlayerState(user.userId, shapes[players.size])))
    }

    fun requestStart(user: User) {
        if(user.userId == owner.userId) {
            processMove(PentaMove.InitGame)
        }
    }
}