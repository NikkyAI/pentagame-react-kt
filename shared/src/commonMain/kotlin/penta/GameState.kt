package penta

import io.data2viz.geom.Point
import mu.KotlinLogging
import penta.logic.Piece
import penta.logic.field.AbstractField

open class GameState {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

//    val players: WrapperObservableList<PlayerState> = mutableObservableListOf()
//    val scoringColors: WrapperObservableMap<String, List<PentaColor>> = WrapperObservableMap()
//
//    val observersProperty: WrapperObservableList<String> = mutableObservableListOf()
//
//    // player id to team id
//    // TODO: unused
//    lateinit var teams: Map<String, Int>
//        private set
//
//    /**
//     * all pieces in the game
//     */
//    var figures: Array<Piece> = arrayOf()
//        private set
//
//    private val blacks: List<Piece.BlackBlocker>
//    private val greys: List<Piece.GrayBlocker>
//    private val positions: MutableMap<String, AbstractField?> = mutableMapOf()
//    val figurePositions: Map<String, AbstractField?> get() = positions
//
//    private val mutableHistory: MutableObservableList<PentaMove> = mutableObservableListOf<PentaMove>().apply {
//        onListAdd.add { it, index ->
//            logger.info { "history added $it" }
//        }
//    }
//    val history: ObservableList<PentaMove> = mutableHistory
//
//    val gameStartedProperty = StandardObservableProperty(false)
//    val gameStarted: Boolean
//        get() = gameStartedProperty.value
//
//    val currentPlayerProperty = StandardObservableProperty(PlayerState("ghost", "circle"))
//    val currentPlayer: PlayerState inline get() = currentPlayerProperty.value
//
//    var isPlayback = false
//
//    val turnProperty: StandardObservableProperty<Int> = StandardObservableProperty(0).apply {
//        add {
//            logger.debug { "set turn = $it" }
//            if (gameStarted) {
//                currentPlayerProperty.value =
//                    if (players.isNotEmpty()) players[it % players.size] else throw IllegalStateException("no turn")
//                updateBoard()
//            }
//
//        }
//    }
//    var turn: Int
//        inline get() = turnProperty.value
//        private inline set(value) {
//            turnProperty.value = value
//        }
//
//    var forceMoveNextPlayer: Boolean = false
//        private set
//
//    var winner: String? = null
//        private set
//
//    var selectedPlayerPiece: Piece.Player? = null
//        protected set
//    var selectedBlackPiece: Piece.BlackBlocker? = null
//        protected set
//    var selectedGrayPiece: Piece.GrayBlocker? = null
//        protected set
//
//    /**
//     * true when no gray pieces are in the middle and one from the board has to be be selected
//     */
//    var selectingGrayPiece: Boolean = false
//        protected set

    init {
//        blacks = (0 until 5).map { i ->
//            Piece.BlackBlocker(
//                "b$i",
//                Point(0.0, 0.0),
//                PentaMath.s / 2.5,
//                PentaColor.values()[i],
//                PentaBoard.j[i]
//            ).also {
//                it.position = it.originalPosition
//            }
//        }
//        greys = (0 until 5).map { i ->
//            Piece.GrayBlocker(
//                "g$i",
//                Point(0.0, 0.0),
//                PentaMath.s / 2.5,
//                PentaColor.values()[i]
//            ).also {
//                it.position = null
//            }
//        }
//        figures = arrayOf(*blacks.toTypedArray(), *greys.toTypedArray()) // , *playerPieces.toTypedArray())
    }

//    private fun defaultHandleIllegalMove(move: PentaMove.IllegalMove) {
//        logger.error {
//            "illegal move: $move"
//        }
//    }
//
//    open fun processMove(
//        move: PentaMove,
//        handleIllegalMove: (PentaMove.IllegalMove) -> Unit = ::defaultHandleIllegalMove
//    ) {
//        try {
//            logger.info { "turn: $turn" }
//            logger.info { "currentPlayer: $currentPlayer" }
//            logger.info { "processing $move" }
//            when (move) {
//                is PentaMove.MovePlayer -> {
//                    requireMove(gameStarted) {
//                        PentaMove.IllegalMove(
//                            "game is not started yet",
//                            move
//                        )
//                    }
//                    requireMove(move.playerPiece.playerId == currentPlayer.id) {
//                        PentaMove.IllegalMove(
//                            "move is not from currentPlayer: ${currentPlayer.id}",
//                            move
//                        )
//                    }
//                    requireMove(canMove(move.from, move.to)) {
//                        PentaMove.IllegalMove(
//                            "no path between ${move.from.id} and ${move.to.id}",
//                            move
//                        )
//                    }
//
//                    val piecesOnTarget = positions
//                        .filterValues {
//                            it == move.to
//                        }.keys
//                        .mapNotNull { id ->
//                            figures.find {
//                                it.id == id
//                            }
//                        }
//
//                    if (piecesOnTarget.size > 1) {
//                        handleIllegalMove(
//                            PentaMove.IllegalMove(
//                                "multiple pieces on target field ${move.to.id}",
//                                move
//                            )
//                        )
//                        return
//                    }
//
//                    val pieceOnTarget = piecesOnTarget.firstOrNull()
//
//                    if (pieceOnTarget != null) {
//                        when (pieceOnTarget) {
//                            is Piece.GrayBlocker -> {
//                                logger.info { "taking ${pieceOnTarget.id} off the board" }
//                                pieceOnTarget.position = null
//                                updatePiecesAtPos(null)
//                            }
//                            is Piece.BlackBlocker -> {
//                                selectedBlackPiece = pieceOnTarget
//                                pieceOnTarget.position = null // TODO: set corner field
//                                updatePiecesAtPos(null)
//                                logger.info { "holding ${pieceOnTarget.id} for repositioning" }
//                            }
//                            else -> {
//                                requireMove(false) {
//                                    PentaMove.IllegalMove(
//                                        "cannot click on piece type: ${pieceOnTarget::class.simpleName}",
//                                        move
//                                    )
//                                }
//                            }
//                        }
//                        // TODO: can unset when pieces float on pointer
////                    pieceOnTarget.position = null
//                    } else {
//                        // no piece on target field
//                    }
//
//                    move.playerPiece.position = move.to
//
//                    postProcess(move)
//
//                    updatePiecesAtPos(move.from)
//                    updatePiecesAtPos(move.to)
//
//                    updateBoard()
//
//                    logger.info { "append history" }
//                    mutableHistory += move
//                }
//                is PentaMove.ForcedPlayerMove -> {
//                    requireMove(gameStarted) {
//                        PentaMove.IllegalMove(
//                            "game is not started yet",
//                            move
//                        )
//                    }
//                    requireMove(move.playerPiece.playerId == currentPlayer.id) {
//                        PentaMove.IllegalMove(
//                            "move is not from currentPlayer: ${currentPlayer.id}",
//                            move
//                        )
//                    }
//                    val sourceField = move.from
//                    if (sourceField is GoalField && move.playerPiece.pentaColor == sourceField.pentaColor) {
//                        postProcess(move)
//                    } else {
//                        requireMove(false) {
//                            PentaMove.IllegalMove(
//                                "Forced player move cannot happen",
//                                move
//                            )
//                        }
//                    }
//                    mutableHistory += move
//                }
//                is PentaMove.SwapOwnPiece -> {
//                    requireMove(gameStarted) {
//                        PentaMove.IllegalMove(
//                            "game is not started yet",
//                            move
//                        )
//                    }
//                    requireMove(canMove(move.from, move.to)) {
//                        PentaMove.IllegalMove(
//                            "no path between ${move.from.id} and ${move.to.id}",
//                            move
//                        )
//                    }
//                    requireMove(move.playerPiece.playerId == currentPlayer.id) {
//                        PentaMove.IllegalMove(
//                            "move is not from currentPlayer: ${currentPlayer.id}",
//                            move
//                        )
//                    }
//                    requireMove(move.playerPiece.position == move.from) {
//                        PentaMove.IllegalMove(
//                            "piece ${move.playerPiece.id} is not on expected position ${move.from.id}",
//                            move
//                        )
//                    }
//                    requireMove(move.otherPlayerPiece.playerId == currentPlayer.id) {
//                        PentaMove.IllegalMove(
//                            "piece ${move.otherPlayerPiece.id} is not from another player",
//                            move
//                        )
//                    }
//                    requireMove(move.otherPlayerPiece.position == move.to) {
//                        PentaMove.IllegalMove(
//                            "piece ${move.otherPlayerPiece.id} is not on expected position ${move.to.id}",
//                            move
//                        )
//                    }
//
//                    move.playerPiece.position = move.to
//                    move.otherPlayerPiece.position = move.from
//                    updatePiecesAtPos(move.to)
//                    updatePiecesAtPos(move.from)
//
//                    postProcess(move)
//                    mutableHistory += move
//                }
//                is PentaMove.SwapHostilePieces -> {
//                    requireMove(gameStarted) {
//                        PentaMove.IllegalMove(
//                            "game is not started yet",
//                            move
//                        )
//                    }
//                    requireMove(canMove(move.from, move.to)) {
//                        PentaMove.IllegalMove(
//                            "no path between ${move.from.id} and ${move.to.id}",
//                            move
//                        )
//
//                    }
//                    requireMove(move.playerPiece.playerId == currentPlayer.id) {
//                        PentaMove.IllegalMove(
//                            "move is not from currentPlayer: ${currentPlayer.id}",
//                            move
//                        )
//                    }
//                    requireMove(move.playerPiece.position == move.from) {
//                        PentaMove.IllegalMove(
//                            "piece ${move.playerPiece.id} is not on expected position ${move.from.id}",
//                            move
//                        )
//                    }
//                    requireMove(move.otherPlayerPiece.playerId != currentPlayer.id) {
//                        PentaMove.IllegalMove(
//                            "piece ${move.otherPlayerPiece.id} is from another player",
//                            move
//                        )
//                    }
//                    requireMove(move.otherPlayerPiece.position == move.to) {
//                        PentaMove.IllegalMove(
//                            "piece ${move.otherPlayerPiece.id} is not on expected position ${move.to.id}",
//                            move
//                        )
//                    }
//                    val lastMove = history.findLast {
//                        it is PentaMove.Move && it.playerPiece.playerId == move.playerPiece.playerId
//                    }
//                    requireMove(lastMove != move) {
//                        PentaMove.IllegalMove(
//                            "repeating move ${move.asNotation()} is illegal",
//                            move
//                        )
//                    }
//
//                    move.playerPiece.position = move.to
//                    move.otherPlayerPiece.position = move.from
//                    updatePiecesAtPos(move.to)
//                    updatePiecesAtPos(move.from)
//                    // TODO
//                    postProcess(move)
//                    mutableHistory += move
//                }
//                is PentaMove.CooperativeSwap -> {
//                    TODO("implement cooperative swap")
//                    postProcess(move)
//                }
//
//                is PentaMove.SetBlack -> {
//                    if (figurePositions.values.any { it == move.to }) {
//                        PentaMove.IllegalMove(
//                            "target position not empty: ${move.to.id}",
//                            move
//                        )
//                    }
//                    requireMove(move.piece.position == null || move.piece.position == move.from) {
//                        PentaMove.IllegalMove(
//                            "illegal source position of ${move.piece.position?.id}",
//                            move
//                        )
//                    }
//
//                    move.piece.position = move.to
//
//                    val lastMove = history.asReversed().find { it !is PentaMove.SetGrey }
//                    if (lastMove !is PentaMove.CanSetBlack) {
//                        PentaMove.IllegalMove(
//                            "last move was not the expected move type: ${lastMove!!::class.simpleName} instead of ${PentaMove.CanSetBlack::class.simpleName}",
//                            move
//                        )
//                    }
//                    mutableHistory += move
//                    selectedBlackPiece = null
//
//                    updatePiecesAtPos(move.to)
//                    updatePiecesAtPos(move.from)
//                }
//                is PentaMove.SetGrey -> {
//                    if (positions.values.any { it == move.to }) {
//                        handleIllegalMove(
//                            PentaMove.IllegalMove(
//                                "target position not empty: ${move.to.id}",
//                                move
//                            )
//                        )
//                        return
//                    }
//                    requireMove(move.piece.position == move.from) {
//                        PentaMove.IllegalMove(
//                            "source and target position are the same: ${move.from?.id}",
//                            move
//                        )
//                    }
//                    logger.debug { "selected: $selectedGrayPiece" }
//                    if (selectingGrayPiece) {
//                        requireMove(selectingGrayPiece && move.from != null) {
//                            PentaMove.IllegalMove(
//                                "source is null",
//                                move
//                            )
//                        }
//                    } else {
//                        requireMove(selectedGrayPiece == move.piece) {
//                            PentaMove.IllegalMove(
//                                "piece ${move.piece.id} is not the same as selected gray piece: ${selectedGrayPiece?.id}",
//                                move
//                            )
//                        }
//                    }
//
//                    move.piece.position = move.to
//                    val lastMove = history.asReversed().find { it !is PentaMove.SetBlack }
//                    requireMove(lastMove is PentaMove.CanSetGrey) {
//                        PentaMove.IllegalMove(
//                            "last move was not the expected move type: ${lastMove!!::class.simpleName} instead of ${PentaMove.CanSetGrey::class.simpleName}",
//                            move
//                        )
//                    }
//                    mutableHistory += move
//
//                    selectedGrayPiece = null
//                    updatePiecesAtPos(move.to)
//                    updatePiecesAtPos(move.from)
//                }
//                is PentaMove.PlayerJoin -> {
//                    requireMove(!gameStarted) {
//                        PentaMove.IllegalMove(
//                            "game is already started",
//                            move
//                        )
//                    }
//                    requireMove(players.none { it.id == move.player.id }) {
//                        PentaMove.IllegalMove(
//                            "player already joined",
//                            move
//                        )
//                    }
//                    players += move.player
//
//                    val playerPieces = (0 until players.size).flatMap { p ->
//                        (0 until 5).map { i ->
//                            Piece.Player(
//                                "p$p$i",
//                                players[p].id,
//                                players[p].figureId,
//                                Point(0.0, 0.0),
//                                PentaMath.s / 2.3,
//                                PentaColor.values()[i]
//                            ).also {
//                                it.position = PentaBoard.c[i]
//                            }
//                        }
//                    }
//                    figures =
//                        arrayOf<Piece>(*blacks.toTypedArray(), *greys.toTypedArray(), *playerPieces.toTypedArray())
//
//                    resetPlayers()
//                    updateAllPieces()
//                    mutableHistory += move
//                }
//                is PentaMove.InitGame -> {
//                    requireMove(!gameStarted) {
//                        PentaMove.IllegalMove(
//                            "game is already started",
//                            move
//                        )
//                    }
//                    // TODO: setup UI for players related stuff here
//
//                    // remove old player pieces positions
////                    figures.filterIsInstance<Piece.Player>().forEach {
////                        positions.remove(it.id)
////                    }
////                    figures.filterIsInstance<Piece.BlackBlocker>().forEach {
////                        it.position = it.originalPosition
////                    }
////                    figures.filterIsInstance<Piece.GrayBlocker>().forEach {
////                        it.position = null
////                    }
//                    gameStartedProperty.value = true
//                    turn = 0
//
////                    resetBoard()
//                    updateAllPieces()
//
//                    mutableHistory += move
//
//                    logger.info { "after init: " + figures.joinToString { it.id } }
//                }
//                is PentaMove.Win -> {
//                    // TODO handle win
//                    mutableHistory += move
//                }
//                is PentaMove.IllegalMove -> {
//                    handleIllegalMove(move)
//                }
//            }.exhaustive
//
//            checkWin()
//
//            if (selectedBlackPiece == null && selectedGrayPiece == null && !selectingGrayPiece
//                && move !is PentaMove.InitGame
//                && move !is PentaMove.PlayerJoin
//                && move !is PentaMove.Win
//                && winner == null
//            ) {
//                turn += 1
//            }
////        if(forceMoveNextPlayer) {
//            forceMovePlayerPiece(currentPlayer)
////        }
//
//            updateBoard()
//        } catch (e: IllegalMoveException) {
//            handleIllegalMove(e.move)
//        } catch (e: Exception) {
//            logger.error(e) { "error" }
//        }
//    }
//
//    /**
//     * check if the moved piece is on a exit point
//     */
//    private fun postProcess(move: PentaMove.Move) {
//        selectedPlayerPiece = null
//
//        val targetField = move.to
//        logger.debug { "playerColor = ${move.playerPiece.pentaColor}" }
//        logger.debug {
//            if (targetField is GoalField) {
//                "pentaColor = ${targetField.pentaColor}"
//            } else {
//                "not a JointField"
//            }
//        }
//        if (targetField is GoalField && targetField.pentaColor == move.playerPiece.pentaColor) {
//            // take piece off the board
//            move.playerPiece.position = null
//
//            val colors = figures
//                .filterIsInstance<Piece.Player>()
//                .filter { it.playerId == move.playerPiece.playerId }
//                .filter { figurePositions[it.id] == null }
//                .map { it.pentaColor }
//            scoringColors[move.playerPiece.playerId] = colors
//
//            // set gamestate to `MOVE_GREY`
//            selectedGrayPiece = positions.filterValues { it == null }
//                .keys.map { id -> figures.find { it.id == id } }
//                .filterIsInstance<Piece.GrayBlocker>()
//                .firstOrNull()
//            if (selectedGrayPiece == null) {
//                selectingGrayPiece = true
//                logger.info { "selected gray piece: ${selectedGrayPiece?.id}" }
//            }
//            updatePiecesAtPos(null)
//        }
//    }
//
//    private fun forceMovePlayerPiece(player: PlayerState) {
//        if (selectingGrayPiece || selectingGrayPiece) return
//        val playerPieces = figures.filterIsInstance<Piece.Player>().filter { it.playerId == player.id }
//        for (playerPiece in playerPieces) {
//            val field = playerPiece.position as? GoalField ?: continue
//            if (field.pentaColor != playerPiece.pentaColor) continue
//
//            // TODO: use extra move type to signal forced move ?
//
//            processMove(
//                PentaMove.ForcedPlayerMove(
//                    playerPiece = playerPiece,
//                    from = field,
//                    to = field
//                )
//            )
//            return
//        }
//    }
//
//    private fun checkWin() {
//        // check win after last players turn
//        if (winner != null || turn % players.size != players.size - 1) return
//        if (selectingGrayPiece || selectedGrayPiece != null || selectedBlackPiece != null) return
//        val winners = players.filter { player ->
//            val playerPieces = figures.filterIsInstance<Piece.Player>().filter { it.playerId == player.id }
//            val offBoardPieces = playerPieces.filter { it.position == null }
//
//            offBoardPieces.size >= 3
//        }
//        if (winners.isNotEmpty()) {
//            winner = winners.joinToString(", ") { it.id }
//            processMove(PentaMove.Win(winners.map { it.id }))
//        }
//    }
//
//    fun canMove(start: AbstractField, end: AbstractField): Boolean {
//        val backtrack = mutableSetOf<AbstractField>()
//        val next = mutableSetOf<AbstractField>(start)
//        while (next.isNotEmpty()) {
//            val toIterate = next.toList()
//            next.clear()
//            toIterate.map { nextField ->
//                logger.debug { "checking: ${nextField.id}" }
//                nextField.connected.forEach { connectedField ->
//                    if (connectedField == end) return true
//                    if (connectedField in backtrack) return@forEach
//
//                    val free = positions.filterValues { field ->
//                        field == connectedField
//                    }.isEmpty()
//                    if (!free) return@forEach
//
//                    if (connectedField !in backtrack) {
//                        backtrack += connectedField
//                        next += connectedField
//                    }
//                }
//            }
//        }
//
//        return false
//    }
//
//    var Piece.position: AbstractField?
//        get() = positions[id]
//        private set(value) {
//            logger.debug { "move $id to ${value?.id}" }
//            positions[id] = value
//        }

//    fun resetPiecePositions() {
//        figures.filterIsInstance<Piece.Player>().forEach {
//            positions.remove(it.id)
//        }
//        figures.filterIsInstance<Piece.BlackBlocker>().forEach {
//            it.position = it.originalPosition
//        }
//        figures.filterIsInstance<Piece.GrayBlocker>().forEach {
//            it.position = null
//        }
//    }

    protected open fun resetPlayers() {}

//    protected open fun updateAllPieces() {
//        figures.forEach { piece ->
//            updatePiecePos(piece)
//        }
//    }

    protected open fun updatePiecePos(piece: Piece): Point { return Point(.0,.0) }
    protected open fun updatePiecesAtPos(field: AbstractField?) {}
    protected open fun updateBoard() {}
}