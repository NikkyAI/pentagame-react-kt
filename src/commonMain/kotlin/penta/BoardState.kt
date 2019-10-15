package penta

import PentaBoard
import PentaMath
import com.lightningkite.reacktive.list.MutableObservableList
import com.lightningkite.reacktive.list.ObservableList
import io.data2viz.geom.Point
import kotlinx.serialization.list
import penta.logic.field.AbstractField
import penta.logic.field.JointField
import penta.logic.Piece
import penta.util.exhaustive
import com.lightningkite.reacktive.list.WrapperObservableList
import com.lightningkite.reacktive.list.mutableObservableListOf
import com.lightningkite.reacktive.property.StandardObservableProperty
import mu.KotlinLogging

open class BoardState() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    var updateLogPanel: (String) -> Unit = {}

    val players: WrapperObservableList<PlayerState> = mutableObservableListOf(PlayerState("initial", "triangle"))

    // player id to team id
    lateinit var teams: Map<String, Int>
        private set

    var figures: Array<Piece> = arrayOf()
        private set

    private val blacks: List<Piece.BlackBlocker>
    private val greys: List<Piece.GrayBlocker>
    private val positions: MutableMap<String, AbstractField?> = mutableMapOf()
    val figurePositions: Map<String, AbstractField?> get() = positions

    private val mutableHistory: MutableObservableList<PentaMove> = mutableObservableListOf<PentaMove>().apply {
        onListAdd.add { it, index ->
            logger.info { "history added $it" }
        }
    }
    val history: ObservableList<PentaMove> = mutableHistory

    var initialized: Boolean = false
        private set

    val currentPlayerProperty = StandardObservableProperty(players.first())
    val currentPlayer: PlayerState inline get() = currentPlayerProperty.value

    val turnProperty: StandardObservableProperty<Int> = StandardObservableProperty(0).apply {
        add {
            logger.debug {"set turn = $it"}
            currentPlayerProperty.value = if (players.isNotEmpty()) players[it % players.size] else throw IllegalStateException("no turn")
            updateBoard()
        }
    }
    var turn: Int
        inline get() = turnProperty.value
        private inline set(value) {
            turnProperty.value = value
        }

    var forceMoveNextPlayer: Boolean = false
        private set

    var winner: String? = null
        private set

    var selectedPlayerPiece: Piece.Player? = null
        protected set
    open var selectedBlackPiece: Piece.BlackBlocker? = null
        protected set
    open var selectedGrayPiece: Piece.GrayBlocker? = null
        protected set

    /**
     * true when no gray pieces are in the middle and one from the board can be selected
     */
    var selectingGrayPiece: Boolean = false
        protected set

    init {
        blacks = (0 until 5).map { i ->
            Piece.BlackBlocker(
                "b$i",
                Point(0.0, 0.0),
                PentaMath.s / 2.5,
                PentaColor.values()[i]
            ).also {
                it.position = PentaBoard.j[i]
            }
        }
        greys = (0 until 5).map { i ->
            Piece.GrayBlocker(
                "g$i",
                Point(0.0, 0.0),
                PentaMath.s / 2.5,
                PentaColor.values()[i]
            ).also {
                it.position = null
            }
        }
        figures = arrayOf(*blacks.toTypedArray(), *greys.toTypedArray()) // , *playerPieces.toTypedArray())
    }

    inline fun requires(b: Boolean, error: () -> Unit) {
        if(!b) error()
    }

    private fun defaultHandleIllegalMove(illegalMove: PentaMove.IllegalMove) {
        logger.error {
            "illegal move: " + illegalMove
        }
    }

    open fun processMove(
        move: PentaMove,
        handleIllegalMove: (PentaMove.IllegalMove)-> Unit = ::defaultHandleIllegalMove
    ) {
        try {
            logger.info { "turn: $turn" }
            logger.info { "currentPlayer: $currentPlayer" }
            logger.info { "processing $move" }
            when (move) {
                is PentaMove.MovePlayer -> {
                    require(move.playerPiece.playerId == currentPlayer.id) { "signal illegal move" }
                    if (!canMove(move.from, move.to)) {
                        handleIllegalMove(PentaMove.IllegalMove(
                            "move is not from currentPlayer: ${currentPlayer.id}",
                            move
                        ))
                        return
                    }

                    val piecesOnTarget = positions
                        .filterValues {
                            it == move.to
                        }.keys
                        .mapNotNull { id ->
                            figures.find {
                                it.id == id
                            }
                        }

                    if (piecesOnTarget.size > 1) {
                        handleIllegalMove(
                            PentaMove.IllegalMove(
                                "multiple pieces on target field ${move.to.id}",
                                move
                            )
                        )
                        return
                    }

                    val pieceOnTarget = piecesOnTarget.firstOrNull()

                    if (pieceOnTarget != null) {
                        when (pieceOnTarget) {
                            is Piece.GrayBlocker -> {
                                logger.info { "taking ${pieceOnTarget.id} off the board" }
                                pieceOnTarget.position = null
                                updatePiecesAtPos(null)
                            }
                            is Piece.BlackBlocker -> {
                                selectedBlackPiece = pieceOnTarget
                                pieceOnTarget.position = null // TODO: set corner field
                                updatePiecesAtPos(null)
                                logger.info { "holding ${pieceOnTarget.id} for repositioning" }
                            }
                            else -> {
                                handleIllegalMove(
                                    PentaMove.IllegalMove(
                                        "cannot click on piece type: ${pieceOnTarget::class.simpleName}",
                                        move
                                    )
                                )
                                return
                            }
                        }
                        // TODO: can unset when pieces float on pointer
//                    pieceOnTarget.position = null
                    } else {
                        // no piece on target field
                    }

                    move.playerPiece.position = move.to

                    postProcess(move)

                    logger.info { "all figures: " + figures.joinToString { it.id } }

                    updatePiecesAtPos(move.to)
                    updatePiecesAtPos(move.from)

                    logger.info { "append history" }
                    mutableHistory += move
                }
                is PentaMove.ForcedPlayerMove -> {
                    requires(move.playerPiece.playerId == currentPlayer.id) {
                        handleIllegalMove(
                            PentaMove.IllegalMove(
                                "move is not from currentPlayer: ${currentPlayer.id}",
                                move
                            )
                        )
                        return
                    }
                    val sourceField = move.from
                    if (sourceField is JointField && move.playerPiece.pentaColor == sourceField.pentaColor) {
                        postProcess(move)
                    } else {
                        handleIllegalMove(
                            PentaMove.IllegalMove(
                                "Forced player move cannot happen",
                                move
                            )
                        )
                        return
                    }
                    mutableHistory += move
                }
                is PentaMove.SwapOwnPiece -> {
                    requires(canMove(move.from, move.to)) {
                        handleIllegalMove(PentaMove.IllegalMove(
                            "no path between ${move.from.id} and ${move.to.id}",
                            move
                        ))
                        return
                    }
                    requires(move.playerPiece.playerId == currentPlayer.id) {
                        handleIllegalMove(PentaMove.IllegalMove(
                            "move is not from currentPlayer: ${currentPlayer.id}",
                            move
                        ))
                        return
                    }
                    requires(move.playerPiece.position == move.from) {
                        handleIllegalMove(PentaMove.IllegalMove(
                            "piece ${move.playerPiece.id} is not on expected position ${move.from.id}",
                            move
                        ))
                        return
                    }
                    requires(move.otherPlayerPiece.playerId == currentPlayer.id) {
                        handleIllegalMove(PentaMove.IllegalMove(
                            "piece ${move.otherPlayerPiece.id} is not from another player",
                            move
                        ))
                        return
                    }
                    requires(move.otherPlayerPiece.position == move.to) {
                        handleIllegalMove(PentaMove.IllegalMove(
                            "piece ${move.otherPlayerPiece.id} is not on expected position ${move.to.id}",
                            move
                        ))
                        return
                    }

                    move.playerPiece.position = move.to
                    move.otherPlayerPiece.position = move.from
                    updatePiecesAtPos(move.to)
                    updatePiecesAtPos(move.from)

                    postProcess(move)
                    mutableHistory += move
                }
                is PentaMove.SwapHostilePieces -> {
                    requires(canMove(move.from, move.to)) {
                        handleIllegalMove(PentaMove.IllegalMove(
                            "no path between ${move.from.id} and ${move.to.id}",
                            move
                        ))
                        return
                    }
                    requires(move.playerPiece.playerId == currentPlayer.id) {
                        handleIllegalMove(PentaMove.IllegalMove(
                            "move is not from currentPlayer: ${currentPlayer.id}",
                            move
                        ))
                        return
                    }
                    requires(move.playerPiece.position == move.from) {
                        handleIllegalMove(PentaMove.IllegalMove(
                            "piece ${move.playerPiece.id} is not on expected position ${move.from.id}",
                            move
                        ))
                        return
                    }
                    requires(move.otherPlayerPiece.playerId != currentPlayer.id) {
                        handleIllegalMove(PentaMove.IllegalMove(
                            "piece ${move.otherPlayerPiece.id} is from another player",
                            move
                        ))
                        return
                    }
                    requires(move.otherPlayerPiece.position == move.to) {
                        handleIllegalMove(PentaMove.IllegalMove(
                            "piece ${move.otherPlayerPiece.id} is not on expected position ${move.to.id}",
                            move
                        ))
                        return
                    }

                    move.playerPiece.position = move.to
                    move.otherPlayerPiece.position = move.from
                    updatePiecesAtPos(move.to)
                    updatePiecesAtPos(move.from)
                    // TODO
                    postProcess(move)
                    mutableHistory += move
                }
                is PentaMove.CooperativeSwap -> {
                    TODO("implement cooperative swap")
                    postProcess(move)
                }

                is PentaMove.SetBlack -> {
                    if (figurePositions.values.any { it == move.to }) {
                        handleIllegalMove(PentaMove.IllegalMove(
                            "target position not empty: ${move.to.id}",
                            move
                        ))
                        return
                    }
                    requires(move.piece.position == null || move.piece.position == move.from) {
                        handleIllegalMove(PentaMove.IllegalMove(
                            "illegal source position of ${move.piece.position?.id}",
                            move
                        ))
                        return
                    }

                    move.piece.position = move.to

                    val lastMove = history.asReversed().find { it !is PentaMove.SetGrey }
                    if(lastMove !is PentaMove.CanSetBlack) {
                        handleIllegalMove(PentaMove.IllegalMove(
                            "last move was not the expected move type: ${lastMove!!::class.simpleName} instead of ${PentaMove.CanSetBlack::class.simpleName}",
                            move
                        ))
                        return
                    }
                    mutableHistory += move
                    selectedBlackPiece = null

                    updatePiecesAtPos(move.to)
                    updatePiecesAtPos(move.from)
                }
                is PentaMove.SetGrey -> {
                    if (positions.values.any { it == move.to }) {
                        handleIllegalMove(PentaMove.IllegalMove(
                            "target position not empty: ${move.to.id}",
                            move
                        ))
                        return
                    }
                    requires(move.piece.position == move.from) {
                        handleIllegalMove(PentaMove.IllegalMove(
                            "source and target position are the same: ${move.from?.id}",
                            move
                        ))
                        return
                    }
                    logger.debug { "selected: $selectedGrayPiece" }
                    if (selectingGrayPiece) {
                        requires(selectingGrayPiece && move.from != null) {
                            handleIllegalMove(PentaMove.IllegalMove(
                                "source is null",
                                move
                            ))
                            return
                        }
                    } else {
                        requires(selectedGrayPiece == move.piece) {
                            handleIllegalMove(PentaMove.IllegalMove(
                                "piece ${move.piece.id} is not the same as selected gray piece: ${selectedGrayPiece?.id}",
                                move
                            ))
                            return
                        }
                    }

                    move.piece.position = move.to
                    val lastMove = history.asReversed().find { it !is PentaMove.SetBlack }
                    if(lastMove !is PentaMove.CanSetGrey) {
                        handleIllegalMove(PentaMove.IllegalMove(
                            "last move was not the expected move type: ${lastMove!!::class.simpleName} instead of ${PentaMove.CanSetGrey::class.simpleName}",
                            move
                        ))
                        return
                    }
                    mutableHistory += move

                    selectedGrayPiece = null
                    updatePiecesAtPos(move.to)
                    updatePiecesAtPos(move.from)
                }
                is PentaMove.InitGame -> {
                    // TODO: setup UI for players related stuff here

                    // remove old player pieces positions
                    figures.filterIsInstance<Piece.Player>().forEach {
                        positions.remove(it.id)
                    }
                    initialized = true
                    players.clear()
                    players += move.players // TODO: add players one by one
                    turn = 0
                    // set player count and names from `GameInit`
                    val playerPieces = (0 until move.players.size).flatMap { p ->
                        (0 until 5).map { i ->
                            Piece.Player(
                                "p$p$i",
                                players[p].id,
                                players[p].figureId,
                                Point(0.0, 0.0),
                                PentaMath.s / 2.3,
                                PentaColor.values()[i]
                            ).also {
                                it.position = PentaBoard.c[i]
                            }
                        }
                    }
                    figures = arrayOf<Piece>(*blacks.toTypedArray(), *greys.toTypedArray(), *playerPieces.toTypedArray())
                    //(blacks + greys + playerPieces).toTypedArray<Piece>()

                    // TODO: reset viz and readd pieces
                    resetBoard()
                    updateAllPieces()

                    mutableHistory.clear()
                    mutableHistory += move

                    logger.info { "after init: " + figures.joinToString { it.id } }
                }
                is PentaMove.Win -> {
                    // TODO handle win
                    mutableHistory += move
                    updateBoard()
                }
                is PentaMove.IllegalMove -> {
                    handleIllegalMove(move)
                }
            }.exhaustive

            checkWin()

            if (selectedBlackPiece == null && selectedGrayPiece == null && !selectingGrayPiece
                && move !is PentaMove.InitGame
                && move !is PentaMove.Win
                && winner == null
            ) {
                turn += 1
            }
//        if(forceMoveNextPlayer) {
            forceMovePlayerPiece(currentPlayer)
//        }

            updateBoard()
        }catch (e: Exception) {
            logger.error(e) { "error" }
        }
    }

    /**
     * check if the moved piece is on a exit point
     */
    private fun postProcess(move: PentaMove.Move) {
        selectedPlayerPiece = null

        val targetField = move.to
        logger.debug { "playerColor = ${move.playerPiece.pentaColor}" }
        logger.debug {
            if (targetField is JointField) {
                "pentaColor = ${targetField.pentaColor}"
            } else {
                "not a JointField"
            }
        }
        if (targetField is JointField && targetField.pentaColor == move.playerPiece.pentaColor) {
            // take piece off the board
            move.playerPiece.position = null

            // set gamestate to `MOVE_GREY`
            selectedGrayPiece = positions.filterValues { it == null }
                .keys.map { id -> figures.find { it.id == id } }
                .filterIsInstance<Piece.GrayBlocker>()
                .firstOrNull()
            if (selectedGrayPiece == null) {
                selectingGrayPiece = true
                logger.info {"selected gray piece: ${selectedGrayPiece?.id}" }
            }
            updatePiecesAtPos(null)
        }
    }

    private fun forceMovePlayerPiece(player: PlayerState) {
        if(selectingGrayPiece || selectingGrayPiece) return
        val playerPieces = figures.filterIsInstance<Piece.Player>().filter { it.playerId == player.id }
        for (playerPiece in playerPieces) {
            val field = playerPiece.position as? JointField ?: continue
            if (field.pentaColor != playerPiece.pentaColor) continue

            // TODO: use extra move type to signal forced move ?

            processMove(
                PentaMove.ForcedPlayerMove(
                    playerPiece = playerPiece,
                    from = field,
                    to = field
                )
            )
            return
        }
    }

    private fun checkWin() {
        // check win after last players turn
        if (winner != null || turn % players.size != players.size-1) return
        if(selectingGrayPiece || selectedGrayPiece != null || selectedBlackPiece != null) return
        val winners = players.filter { player ->
            val playerPieces = figures.filterIsInstance<Piece.Player>().filter { it.playerId == player.id }
            val offBoardPieces = playerPieces.filter { it.position == null }

            offBoardPieces.size >= 3
        }
        if(winners.isNotEmpty()) {
            winner = winners.joinToString(", ") { it.id }
            processMove(PentaMove.Win(winners.map { it.id }))
        }
    }

    fun canMove(start: AbstractField, end: AbstractField): Boolean {
        val backtrack = mutableSetOf<AbstractField>()
        val next = mutableSetOf<AbstractField>(start)
        while (next.isNotEmpty()) {
            val toIterate = next.toList()
            next.clear()
            toIterate.map { nextField ->
                logger.debug {"checking: ${nextField.id}"}
                nextField.connected.forEach { connectedField ->
                    if (connectedField == end) return true
                    if (connectedField in backtrack) return@forEach

                    val free = positions.filterValues { field ->
                        field == connectedField
                    }.isEmpty()
                    if (!free) return@forEach

                    if (connectedField !in backtrack) {
                        backtrack += connectedField
                        next += connectedField
                    }
                }
            }
        }

        return false
    }

    var Piece.position: AbstractField?
        get() = positions[id]
        private set(value) {
            positions[id] = value
        }


    protected open fun resetBoard() {}
    protected open fun updateAllPieces() {
        figures.forEach { piece ->
            updatePiecePos(piece)
        }
    }
    protected open fun updatePiecePos(piece: Piece) {}
    protected open fun updatePiecesAtPos(field: AbstractField?) {}
    protected open fun updateBoard() {
        updateLogPanel(
            json.stringify(SerialNotation.serializer().list,
                history.map {
                    it.toSerializable()
                }
            ) + "\n" +
                    history.joinToString("\n") {
                        it.asNotation()
                    }
        )
    }
}