package penta.server

import com.soywiz.klogger.Logger
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.websocket.DefaultWebSocketServerSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.list
import kotlinx.serialization.serializer
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.reduxkotlin.SelectorBuilder
import org.reduxkotlin.Store
import org.reduxkotlin.StoreSubscription
import org.reduxkotlin.applyMiddleware
import org.reduxkotlin.createStore
import penta.BoardState
import penta.PentaMove
import penta.PlayerState
import penta.UserInfo
import penta.logic.GameType
import penta.network.GameEvent
import penta.network.GameSessionInfo
import penta.server.db.Game
import penta.server.db.Games
import penta.util.handler
import penta.util.json
import penta.util.loggingMiddleware
import java.io.IOException

/***
 * represents a pentagame match
 */
class ServerGamestate(
    val serverGameId: String,
    var owner: User
) {
    private val logger = Logger(this::class.simpleName!! + " id: " + serverGameId)
    val boardContext = newSingleThreadContext("board store $serverGameId")
    val sessionContext = newSingleThreadContext("session store $serverGameId")

    private val boardStateStore: Store<BoardState> = runBlocking(boardContext) {
        createStore(
            BoardState.Companion::reduceFunc,
            BoardState.create(),
            applyMiddleware(loggingMiddleware(logger))
        )
    }

    suspend fun getBoardState() = withContext(boardContext) {
        boardStateStore.state
    }

    suspend fun boardDispatch(action: Any) = withContext(boardContext) {
        boardStateStore.dispatch(action)
    }

    private val sessionStore: Store<SessionState> = runBlocking(sessionContext) {
        createStore(
            SessionState.reducer,
            SessionState(),
            applyMiddleware(loggingMiddleware(logger))
        )
    }

//    companion object {
//    }

    val info: GameSessionInfo
        get() {
            val boardState = runBlocking(boardContext) {
                boardStateStore.state
            }
            val sessionState = runBlocking(sessionContext) {
                sessionStore.state
            }
            return GameSessionInfo(
                id = serverGameId,
                owner = owner.userId,
                running = boardState.gameStarted,
                playingUsers = sessionState.playingUsers.map { it.value.displayName }, // TODO: access sessionStore
                observers = runBlocking(sessionContext) {
                    sessionStore.state.observingSessions.keys.map { it.userId }
                }
            )
        }

    private val serializer = GameEvent.serializer()

    suspend fun <T> doForDifferent(original: List<T>, new: List<T>, op: suspend (T)->Unit) {
        new.forEachIndexed { i, e ->
            val other = original.elementAtOrNull(i)
            if(other == null) {
                op(e)
            } else {
                if(other != e) {
                    throw IllegalStateException("history differs")
                }
            }
        }
    }

    suspend fun handle(wss: DefaultWebSocketServerSession, session: UserSession) = with(wss) {
        var unsubscribe: StoreSubscription = {}
        try {
            withContext(sessionContext) {
                sessionStore.dispatch(SessionState.Companion.Actions.AddObserver(session, wss))

                // play back history
                logger.info { "sending observers" }
                outgoing.send(
                    Frame.Text(
                        json.stringify(
                            String.serializer().list,
                            sessionStore.state.observingSessions.keys.map { it.userId })
                    )
                )
            }

            withContext(boardContext) {
                logger.info { "play back history" }
                outgoing.send(
                    Frame.Text(
                        json.stringify(
                            serializer.list,
                            boardStateStore.state.history.map { it.toSerializable() })
                    )
                )
                val sentMoves: MutableList<PentaMove> = boardStateStore.state.history.toMutableList()

                val historySelector = SelectorBuilder<BoardState>()
                    .withSingleField { boardStateStore.state.history }
                val playerSelector = SelectorBuilder<BoardState>()
                    .withSingleField { boardStateStore.state.gameType.players }

                unsubscribe = boardStateStore.subscribe {
                    historySelector.getIfChangedIn(boardStateStore.state)?.let { history ->
                        logger.info { "history triggered change: ${history.size}" }
                        GlobalScope.launch(handler) {
                            doForDifferent(sentMoves, history) { move ->
                                logger.info { "transmitting move $move" }
                                outgoing.send(Frame.Text(json.stringify(serializer, move.toSerializable())))
                                sentMoves += move
                            }
                        }
//                        val moves = history.toList() - sentMoves
//                        if (moves.isEmpty()) {
//                            logger.error { "no difference in moves" }
//                        }
//                        GlobalScope.launch(handler) {
//                            moves.forEach { move ->
//                                logger.info { "transmitting move $move" }
//                                outgoing.send(Frame.Text(json.stringify(serializer, move.toSerializable())))
//                            }
//                        }
//                        sentMoves += moves

                        logger.info { "writing history to db" }
                        transaction {
//                        addLogger(Slf4jSqlDebugLogger)
                            addLogger(StdOutSqlLogger)
                            logger.info { Game.all() }
                            val game = Game.find(Games.gameId eq serverGameId).firstOrNull()
                            if (game == null) {
                                logger.error { "could not find $serverGameId" }
                                return@transaction
                            }

                            game.history = json.stringify(
                                GameEvent.serializer().list,
                                history.map { it.toSerializable() }
                            )
                        }
                    }

                    playerSelector.getIfChangedIn(boardStateStore.state)?.let { players ->
                        logger.info { "players triggered change: $players" }
                        logger.info { "writing players to db" }
                        transaction {
                            addLogger(StdOutSqlLogger)
                            val game = Game.find(Games.gameId eq serverGameId).firstOrNull()
//                        val game = Game.all().find { it.gameId == id }
                            if (game == null) {
                                logger.error { "could not find $serverGameId" }
                                return@transaction
                            }
                            game.players = SizedCollection(
                                players.mapNotNull {
                                    UserManager.findDBUser(it.id)
                                }
                            )
                        }
                    }
                }
            }

            while (true) {
                val notationJson = (incoming.receive() as Frame.Text).readText()
                logger.info { "ws received: $notationJson" }

                val notation = json.parse(serializer, notationJson)
                withContext(boardContext) {
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
                            // reset illegalMoveState
                            boardStateStore.dispatch(BoardState.RemoveIllegalMove)
                        }
                    }
                }
                // apply move ?
            }
        } catch (e: IOException) {
            e.printStackTrace()
            withContext(sessionContext) {
                sessionStore.dispatch(SessionState.Companion.Actions.RemoveObserver(session))
            }
            val reason = closeReason.await()
            logger.debug { e }
            logger.debug { "onClose $reason" }
        } catch (e: ClosedReceiveChannelException) {
            withContext(sessionContext) {
                sessionStore.dispatch(SessionState.Companion.Actions.RemoveObserver(session))
            }
            val reason = closeReason.await()
            logger.error { "onClose $reason" }
        } catch (e: ClosedSendChannelException) {
            withContext(sessionContext) {
                sessionStore.dispatch(SessionState.Companion.Actions.RemoveObserver(session))
            }
            val reason = closeReason.await()
            logger.error { "onClose $reason" }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(sessionContext) {
                sessionStore.dispatch(SessionState.Companion.Actions.RemoveObserver(session))
            }
            logger.error { e }
            logger.error { "exception onClose ${e.message}" }
        } finally {
            unsubscribe()
        }
    }

    suspend fun requestJoin(user: User, shape: String, player: PlayerState) {
        val success = withContext(boardContext) {
            if (boardStateStore.state.players.size >= boardStateStore.state.gameType.playerCount) {
                logger.error { "max player limit reached" }
                return@withContext false
            }
            if (boardStateStore.state.gameStarted) {
                logger.error { "game has already started" }
                return@withContext false
            }
            true
        }
        if(!success) return

        withContext(sessionContext) {
            val existingUser = sessionStore.state.playingUsers[player]
            if (existingUser != null) {
                logger.error { "there is already $existingUser on $player" }
                return@withContext
            }
            if (boardStateStore.state.gameStarted) {
                logger.error { "game has already started" }
                return@withContext
            }
            sessionStore.dispatch(
                SessionEvent.PlayerJoin(
                    player = player,
                    user = UserInfo(
                        id = user.userId,
                        figureId = shape
                    )
                )
            )
        }
//        processMove()
    }

    suspend fun requestStart(user: User) {
        if (user.userId == owner.userId) {
            val playingUsers = withContext(sessionContext) {
                sessionStore.state.playingUsers
            }
            withContext(boardContext) {
                // TODO: count people in session and initialize
                val gameType = when(playingUsers.size) {
                    2 -> GameType.TWO
                    3 -> GameType.THREE
                    4 -> GameType.FOUR
                    else -> error("cannot handle ${playingUsers}")
                }

                boardStateStore.dispatch(
                    PentaMove.InitGame(gameType = gameType)
                )
            }
        }
    }
}
