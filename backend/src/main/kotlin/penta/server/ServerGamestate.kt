package penta.server

import actions.Action
import com.soywiz.klogger.Logger
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
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.reduxkotlin.*
import penta.PentaMove
import penta.PlayerState
import penta.network.GameEvent
import penta.network.GameSessionInfo
import penta.BoardState
import penta.BoardState.Companion.processMove
import penta.server.db.Game
import penta.server.db.Games
import penta.util.handler
import penta.util.json
import penta.util.loggingMiddleware

/***
 * represents a pentagame match
 */
class ServerGamestate(
    val id: String,
    var owner: User
) {
    val boardStateStore: Store<BoardState> = createStore(
        ::reduce,
        BoardState.create(),
        applyMiddleware(loggingMiddleware(logger))
    )

    val sessionStore: Store<SessionState> = createStore(
        SessionState.reducer,
        SessionState(),
        applyMiddleware(loggingMiddleware(logger))
    )

    companion object {
        private val logger = Logger(this::class.simpleName!!)
    }

    // TODO: trigger database updates
    fun reduce(state: BoardState, action:Any): BoardState {
        return when (action) {
//            is org.reduxkotlin.ActionTypes.INIT -> {
//                logger.info { "received INIT" }
//                state
//            }
//            is org.reduxkotlin.ActionTypes.REPLACE -> {
//                logger.info { "received REPLACE" }
//                state
//            }
            is PentaMove -> {
                val oldHistory = state.history
                val oldPlayers = state.players
                val newState = BoardState.reducer(state, action)

                try {
                    if (oldHistory != newState.history) {
                        transaction {
                            addLogger(Slf4jSqlDebugLogger)
                            val game = Game.find(Games.gameId eq id).first()
                            game.history = json.stringify(
                                GameEvent.serializer().list,
                                newState.history.map { it.toSerializable() }
                            )
                        }
                    }

                    if (oldPlayers != newState.players) {
                        transaction {
                            addLogger(Slf4jSqlDebugLogger)
                            val game = Game.find(Games.gameId eq id).first()
                            game.players = SizedCollection(
                                newState.players.mapNotNull {
                                    UserManager.findDBUser(it.id)
                                }
                            )
                        }
                    }
                } catch(e: Exception) {
                    logger.error { e }
                }

                newState
            }
            else -> {
                BoardState.reducer(state, action)
            }
        }
    }

    var running: Boolean = false
//    val observeringSessions = WrapperObservableMap<UserSession, DefaultWebSocketServerSession>().apply {
//        onMapRemove.add { session, new_wss ->
//            GlobalScope.launch(handler) {
//                values.forEach { wss ->
//                    if (wss != new_wss) {
//                        // TODO: wrap Move into object
//                        // TODO: put observer join/leave
////                        wss.outgoing.send(
////                            Frame.Text(
////                                json.stringify(GameEvent.serializer(), GameEvent.ObserverLeave(session.userId))
////                            )
////                        )
//                    }
//                }
//            }
//        }
//        onMapPut.add { session, hadPrevious, _, new_wss ->
//            GlobalScope.launch(handler) {
//                values.forEach { wss ->
//                    if (wss != new_wss) {
//                        // TODO: wrap Move into object
//                        // TODO: put observer join/leave
////                        wss.outgoing.send(
////                            Frame.Text(
////                                json.stringify(GameEvent.serializer(), GameEvent.ObserverJoin(session.userId))
////                            )
////                        )
//                    }
//                }
//            }
//        }
//    }

    val info: GameSessionInfo
        get() {
            return GameSessionInfo(
                id = id,
                owner = owner.userId,
                running = running,
                players = boardStateStore.state.players.map { it.id },
                observers = sessionStore.state.observingSessions.keys.map { it.userId }
            )
        }

    private val serializer = GameEvent.serializer()

    suspend fun handle(websocketSession: DefaultWebSocketServerSession, session: UserSession) = with(websocketSession) {
        var unsubscribe: StoreSubscription = {}
        try {
            sessionStore.dispatch(SessionState.Companion.Actions.AddObserver(session, this))
//            observeringSessions[session] = this
            // play back history
            logger.info { "sending observers" }
            outgoing.send(
                Frame.Text(
                    json.stringify(
                        String.serializer().list,
                        sessionStore.state.observingSessions.keys.map { it.userId })
                )
            )


            logger.info { "play back history" }
            outgoing.send(
                Frame.Text(
                    json.stringify(
                        serializer.list,
                        boardStateStore.state.history.map { it.toSerializable() })
                )
            )
//            history.forEach { move ->
//                logger.info { "transmitting move $move" }
//                outgoing.send(Frame.Text(json.stringify(serializer, move.toSerializable())))
//            }

            var oldHistory: List<PentaMove> = listOf()
            val historySelector = SelectorBuilder<BoardState>()
                .withSingleField({ boardStateStore.state.history })

            unsubscribe = boardStateStore.subscribe {
                val history = historySelector(boardStateStore.state)
                val moves = history.toList() - oldHistory
                GlobalScope.launch(handler) {
                    moves.forEach { move ->
                        logger.info { "transmitting move $move" }
                        outgoing.send(Frame.Text(json.stringify(serializer, move.toSerializable())))
                    }
                }
                oldHistory = boardStateStore.state.history.toList()
            }

            while (true) {
                val notationJson = (incoming.receive() as Frame.Text).readText()
                logger.info { "ws received: $notationJson" }

                val notation = json.parse(serializer, notationJson)
                notation.asMove(boardStateStore.state).also {
                    boardStateStore.dispatch(it)
                    boardStateStore.state.illegalMove?.let { illegalMove ->
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
            sessionStore.dispatch(SessionState.Companion.Actions.RemoveObserver(session))
//            observeringSessions.remove(session)
            val reason = closeReason.await()
            logger.debug { e }
            logger.debug { "onClose $reason" }
        } catch (e: ClosedReceiveChannelException) {
            sessionStore.dispatch(SessionState.Companion.Actions.RemoveObserver(session))
//            observeringSessions.remove(session)
            val reason = closeReason.await()
            logger.error { "onClose $reason" }
        } catch (e: ClosedSendChannelException) {
            sessionStore.dispatch(SessionState.Companion.Actions.RemoveObserver(session))
//            observeringSessions.remove(session)
            val reason = closeReason.await()
            logger.error { "onClose $reason" }
        } catch (e: Exception) {
            sessionStore.dispatch(SessionState.Companion.Actions.RemoveObserver(session))
//            observeringSessions.remove(session)
            logger.error { e }
            logger.error { "exception onClose ${e.message}" }
        } finally {
            unsubscribe()
        }
    }

    fun requestJoin(user: User) {
        if (boardStateStore.state.players.size > 3) {
            logger.error { "max player limit reached" }
            return
        }
        if (boardStateStore.state.gameStarted) {
            logger.error { "game has already started" }
            return
        }

        val shapes = listOf("triangle", "square", "cross", "circle")
        boardStateStore.dispatch(
            PentaMove.PlayerJoin(
                PlayerState(
                    user.userId,
                    shapes[boardStateStore.state.players.size]
                )
            )
        )
//        processMove()
    }

    fun requestStart(user: User) {
        if (user.userId == owner.userId) {
            boardStateStore.dispatch(PentaMove.InitGame)
//            processMove(PentaMove.InitGame)
        }
    }
}
