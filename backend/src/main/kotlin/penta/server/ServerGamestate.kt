package penta.server

import SessionEvent
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
import kotlinx.serialization.map
import kotlinx.serialization.serializer
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.reduxkotlin.SelectorBuilder
import org.reduxkotlin.Store
import org.reduxkotlin.StoreSubscription
import org.reduxkotlin.applyMiddleware
import org.reduxkotlin.createStore
import penta.BoardState
import penta.PentaMove
import penta.PlayerIds
import penta.UserInfo
import penta.network.GameEvent
import penta.network.GameSessionInfo
import penta.server.db.Game
import penta.server.db.Games
import penta.server.db.PlayingUser
import penta.server.db.PlayingUsers
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

    //    val boardContext = newSingleThreadContext("board store $serverGameId")
    val storeContext = newSingleThreadContext("session store $serverGameId")

//    private val boardStateStore: Store<BoardState> = runBlocking(boardContext) {
//        createStore(
//            BoardState.Companion::reduceFunc,
//            BoardState.create(),
//            applyMiddleware(loggingMiddleware(logger))
//        )
//    }

    private val store: Store<SessionState> = runBlocking(storeContext) {
        createStore(
            SessionState.reducer,
            SessionState(),
            applyMiddleware(loggingMiddleware(logger))
        )
    }

    suspend fun getBoardState() = withContext(storeContext) {
        store.state.boardState
    }

    suspend fun boardDispatch(action: Any) = withContext(storeContext) {
        store.dispatch(action)
    }


//    companion object {
//    }

    val info: GameSessionInfo
        get() {
            val boardState = runBlocking(storeContext) {
                store.state.boardState
            }
            val sessionState = runBlocking(storeContext) {
                store.state
            }
            return GameSessionInfo(
                id = serverGameId,
                owner = owner.userId,
                running = boardState.gameStarted,
                playingUsers = sessionState.playingUsers.mapValues {
                    UserInfo(
                        it.value.user.displayName,
                        it.value.figureId
                    )
                }, // TODO: access sessionStore
                observers = runBlocking(storeContext) {
                    store.state.observingSessions.keys.map { it.userId }
                }
            )
        }

    suspend fun <T> doForDifferent(original: List<T>, new: List<T>, op: suspend (T) -> Unit) {
        new.forEachIndexed { i, e ->
            val other = original.elementAtOrNull(i)
            if (other == null) {
                op(e)
            } else {
                if (other != e) {
                    throw IllegalStateException("history differs")
                }
            }
        }
    }

    suspend fun handle(wss: DefaultWebSocketServerSession, session: UserSession) = with(wss) {
        var unsubscribe: StoreSubscription = {}
        try {
            // TODO: sync playing users
            withContext(storeContext) {
                store.dispatch(SessionState.Companion.Actions.AddObserver(session, wss))

                // sending joined playersplayingUsers
                // TODO: turn this into a message type
                outgoing.send(
                    Frame.Text(
                        json.stringify(
//                            (PlayerIds.serializer() to UserInfo.serializer()).map,
                            (PlayerIds.Companion to UserInfo.serializer()).map,
                            store.state.playingUsers.map { (player, userInfo) ->
                                player to userInfo.toUserInfo(session)
                            }.toMap()
                        )
                    )
                )
                // play back history
                logger.info { "sending observers" }
                // TODO: turn this into a message type
                outgoing.send(
                    Frame.Text(
                        json.stringify(
                            String.serializer().list,
                            store.state.observingSessions.keys.map { it.userId })
                    )
                )
            }

            withContext(storeContext) {
                logger.info { "play back history" }
                outgoing.send(
                    Frame.Text(
                        json.stringify(
                            GameEvent.serializer().list,
                            store.state.boardState.history.map { it.toSerializable() })
                    )
                )
                val sentMoves: MutableList<PentaMove> = store.state.boardState.history.toMutableList()
                val oldPlayingusers: MutableMap<PlayerIds, ServerUserInfo> = store.state.playingUsers.toMutableMap()

                val historySelector = SelectorBuilder<BoardState>()
                    .withSingleField { store.state.boardState.history }
                val playerSelector = SelectorBuilder<BoardState>()
                    .withSingleField { store.state.playingUsers }

                unsubscribe = store.subscribe {
                    historySelector.getIfChangedIn(store.state.boardState)?.let { history ->
                        logger.info { "history triggered change: ${history.size}" }
                        GlobalScope.launch(handler) {
                            doForDifferent(sentMoves, history) { move ->
                                logger.info { "transmitting move $move" }
                                outgoing.send(
                                    Frame.Text(
                                        json.stringify(
                                            SessionEvent.serializer(),
                                            SessionEvent.WrappedGameEvent(
                                                event = move.toSerializable()
                                            )
                                        )
                                    )
                                )
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

                    playerSelector.getIfChangedIn(store.state.boardState)?.let { playingUsers ->
                        logger.info { "players triggered change: $playingUsers" }

                        val removedUsers = oldPlayingusers - playingUsers.keys
                        val newUsers = playingUsers - oldPlayingusers.keys
                        val notChangedUsers = playingUsers - newUsers.keys
                        GlobalScope.launch(handler) {
                            notChangedUsers.forEach {(player, info) ->
                                val oldInfo = oldPlayingusers[player]!!
                                if(info != oldInfo) {
                                    outgoing.send(Frame.Text(json.stringify(SessionEvent.serializer(), SessionEvent.PlayerLeave(player, oldInfo.toUserInfo(session)))))
                                    outgoing.send(Frame.Text(json.stringify(SessionEvent.serializer(), SessionEvent.PlayerJoin(player, info.toUserInfo(session)))))
                                }
                            }
                            removedUsers.forEach { (player, info) ->
                                outgoing.send(Frame.Text(json.stringify(SessionEvent.serializer(), SessionEvent.PlayerLeave(player, info.toUserInfo(session)))))
                            }
                            newUsers.forEach { (player, info) ->
                                outgoing.send(Frame.Text(json.stringify(SessionEvent.serializer(), SessionEvent.PlayerJoin(player, info.toUserInfo(session)))))
                            }
                        }

                        logger.info { "writing players to db" }
                        transaction {
                            addLogger(StdOutSqlLogger)
                            val game = Game.find(Games.gameId eq serverGameId).limit(1).firstOrNull()
                            if (game == null) {
                                logger.error { "could not find $serverGameId" }
                                rollback()
                                return@transaction
                            }
                            val playersInGames = PlayingUser.find(PlayingUsers.gameId eq game.id).toList()
                            game.playingUsers = SizedCollection(
                                playingUsers.mapNotNull { (player, info) ->
//                                    UserManager.findDBUser(info.)
                                    val playerInGame = playersInGames.find { it.game == game && it.player == player.id }
                                    if (playerInGame != null) {
                                        playerInGame.user = UserManager.toDBUser(info.user)
                                        playerInGame.shape = info.figureId
                                        playerInGame
                                    } else {
                                        PlayingUser.new {
                                            this.game = game
                                            this.player = player.id
                                            this.user = UserManager.toDBUser(info.user)
                                            this.shape = info.figureId
                                        }
                                    }

                                }
                            )
                        }
                    }
                }
            }

            while (true) {
                val notationJson = (incoming.receive() as Frame.Text).readText()
                logger.info { "ws received: $notationJson" }

                val sessionEvent = json.parse(SessionEvent.serializer(), notationJson)
                withContext(storeContext) {
                    val authedSessionEvent = AuthedSessionEvent(
                        event = sessionEvent,
                        user = session.asUser()
                    )
                    store.dispatch(authedSessionEvent)

                    // handle possible illegal moves
                    store.state.boardState.illegalMove?.let { illegalMove ->
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
                        store.dispatch(BoardState.RemoveIllegalMove)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            withContext(storeContext) {
                store.dispatch(SessionState.Companion.Actions.RemoveObserver(session))
            }
            val reason = closeReason.await()
            logger.debug { e }
            logger.debug { "onClose $reason" }
        } catch (e: ClosedReceiveChannelException) {
            withContext(storeContext) {
                store.dispatch(SessionState.Companion.Actions.RemoveObserver(session))
            }
            val reason = closeReason.await()
            logger.error { "onClose $reason" }
        } catch (e: ClosedSendChannelException) {
            withContext(storeContext) {
                store.dispatch(SessionState.Companion.Actions.RemoveObserver(session))
            }
            val reason = closeReason.await()
            logger.error { "onClose $reason" }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(storeContext) {
                store.dispatch(SessionState.Companion.Actions.RemoveObserver(session))
            }
            logger.error { e }
            logger.error { "exception onClose ${e.message}" }
        } finally {
            unsubscribe()
        }
    }

    suspend fun requestJoin(user: User, shape: String, player: PlayerIds) {
        val success = withContext(storeContext) {
            if (store.state.boardState.gameType.players.size >= store.state.boardState.gameType.playerCount) {
                logger.error { "max player limit reached" }
                return@withContext false
            }
            if (store.state.boardState.gameStarted) {
                logger.error { "game has already started" }
                return@withContext false
            }
            true
        }
        if (!success) return

        withContext(storeContext) {
            val existingUser = store.state.playingUsers[player]
            if (existingUser != null) {
                logger.error { "there is already $existingUser on $player" }
                return@withContext
            }
            if (store.state.boardState.gameStarted) {
                logger.error { "game has already started" }
                return@withContext
            }
            store.dispatch(
                SessionEvent.PlayerJoin(
                    player = player,
                    user = UserInfo(
                        userId = user.userId,
                        figureId = shape
                    )
                )
            )
        }
//        processMove()
    }

    suspend fun requestStart(user: User) {
        if (user.userId == owner.userId) {
//            val playingUsers = withContext(sessionContext) {
//                sessionStore.state.playingUsers
//            }
            withContext(storeContext) {
                // TODO: count people in session and initialize
//                val gameType = when(playingUsers.size) {
//                    2 -> GameType.TWO
//                    3 -> GameType.THREE
//                    4 -> GameType.FOUR
//                    else -> error("cannot handle ${playingUsers}")
//                }

                store.dispatch(
                    PentaMove.InitGame
                )
            }
        }
    }

    suspend fun Transaction.loadUsers(game: Game) {
        withContext(storeContext) {
            game.playingUsers.forEach {
                store.dispatch(
                    AuthedSessionEvent(
                        SessionEvent.PlayerJoin(PlayerIds.valueOf(it.player), UserInfo(it.user.displayName ?: it.user.userId, it.shape)),
                        UserManager.convert(it.user)
                    )
                )
            }
        }
    }
}
