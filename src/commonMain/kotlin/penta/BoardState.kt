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
import penta.util.replaceLast

open class BoardState() {
    var updateLogPanel: (String) -> Unit = {}

   val players: WrapperObservableList<PlayerState> = mutableObservableListOf(PlayerState("", ""))

    // player id to team id
    lateinit var teams: Map<String, Int>
        private set

    var figures: Array<Piece> = arrayOf()
        private set

    private val blacks: List<Piece.BlackBlocker>
    private val greys: List<Piece.GrayBlocker>
    private val positions: MutableMap<String, AbstractField?> = mutableMapOf()
    val figurePositions: Map<String, AbstractField?> get() = positions

    private val mutableHistory: MutableObservableList<PentaMove> = mutableObservableListOf()
    val history: ObservableList<PentaMove> = mutableHistory

    var initialized: Boolean = false
        private set

    val currentPlayerProperty = StandardObservableProperty(players.first())
    val currentPlayer: PlayerState inline get() = currentPlayerProperty.value

    val turnProperty: StandardObservableProperty<Int> = StandardObservableProperty(0).apply {
        add {
            println("set turn = $it")
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

    fun processMove(move: PentaMove, render: Boolean = true) {
        println("turn: $turn")
        println("currentPlayer: $currentPlayer")
        println("processing $move")
        when (move) {
            is PentaMove.MovePlayer -> {
                require(move.playerPiece.playerId == currentPlayer.id) { "signal illegal move" }
                if (!canMove(move.from, move.to)) {
                    TODO("signal IllegalMove")
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
                    TODO("signal illegal move")
                }

                val pieceOnTarget = piecesOnTarget.firstOrNull()

                if (pieceOnTarget != null) {
                    when(pieceOnTarget) {
                        is Piece.GrayBlocker -> {
                            println("taking ${pieceOnTarget.id} off the board")
                            pieceOnTarget.position = null
                            updatePiecesAtPos(null)
                        }
                        is Piece.BlackBlocker -> {
                            selectedBlackPiece = pieceOnTarget
                            pieceOnTarget.position = null // TODO: set corner field
                            updatePiecesAtPos(null)
                            println("holding ${pieceOnTarget.id} for repositioning")
                        }
                        else -> {
                            TODO("signal illegal move")
                        }
                    }
                    // TODO: can unset when pieces float on pointer
//                    pieceOnTarget.position = null

                } else {
                    // no piece on target field
                }

                move.playerPiece.position = move.to

                postProcess(move)

                updatePiecesAtPos(move.to)
                updatePiecesAtPos(move.from)

                mutableHistory += move
                move.setBlack?.let {
                    processMove(it)
                }
                move.setGrey?.let {
                    processMove(it)
                }
            }
            is PentaMove.ForcedPlayerMove -> {
                requires(move.playerPiece.playerId == currentPlayer.id) { TODO("signal illegal move") }
                val sourceField = move.from
                if(sourceField is JointField && move.playerPiece.pentaColor == sourceField.pentaColor ) {
                    postProcess(move)
                } else {
                    TODO("signal illegal move")
                }
                mutableHistory += move
                move.setGrey?.let {
                    processMove(it)
                }
            }
            is PentaMove.SwapOwnPiece -> {
                requires(canMove(move.from, move.to)) { TODO("signal illegal move") }
                requires(move.playerPiece.playerId == currentPlayer.id) { TODO("signal illegal move") }
                requires(move.playerPiece.position == move.from) { TODO("signal illegal move") }
                requires(move.otherPlayerPiece.playerId == currentPlayer.id) { TODO("signal illegal move") }
                requires(move.otherPlayerPiece.position == move.to) { TODO("signal illegal move") }

                move.playerPiece.position = move.to
                move.otherPlayerPiece.position = move.from
                updatePiecesAtPos(move.to)
                updatePiecesAtPos(move.from)

                postProcess(move)
                mutableHistory += move
                move.setGrey?.let {
                    processMove(it)
                }
            }
            is PentaMove.SwapHostilePieces -> {
                requires(canMove(move.from, move.to)) { TODO("signal illegal move") }
                requires(move.playerPiece.playerId == currentPlayer.id) { TODO("signal illegal move") }
                requires(move.playerPiece.position == move.from) { TODO("signal illegal move") }
                requires(move.otherPlayerPiece.playerId != currentPlayer.id) { TODO("signal illegal move") }
                requires(move.otherPlayerPiece.position == move.to) { TODO("signal illegal move") }

                move.playerPiece.position = move.to
                move.otherPlayerPiece.position = move.from
                updatePiecesAtPos(move.to)
                updatePiecesAtPos(move.from)
                // TODO
                postProcess(move)
                mutableHistory += move
                move.setGrey?.let {
                    processMove(it)
                }
            }
            is PentaMove.CooperativeSwap -> {
                TODO("implement cooperative swap")
                postProcess(move)
            }

            is PentaMove.SetBlack -> {
                if (figurePositions.values.any { it == move.to }) {
                    println("target position not empty")
                    TODO("signal illegal move")
                    return
                }
                requires(move.piece.position == null || move.piece.position == move.from) { TODO("signal illegal move") }

                move.piece.position = move.to
                mutableHistory.replaceLast {
                    require(this is PentaMove.CanSetBlack) { "last move was not the expected move type: $this" }
                    withSetBlack(move)
                }
                selectedBlackPiece = null

                updatePiecesAtPos(move.to)
                updatePiecesAtPos(move.from)
            }
            is PentaMove.SetGrey -> {
                if (positions.values.any { it == move.to }) {
                    println("target position not empty")
                    TODO("signal illegal move")
                    return
                }
                requires(move.piece.position == move.from) { TODO("signal illegal move") }
                println("selected: $selectedGrayPiece")
                if(selectingGrayPiece) {
                    requires(selectingGrayPiece && move.from != null){ TODO("signal illegal move") }
                } else {
                    requires(selectedGrayPiece == move.piece){ TODO("signal illegal move") }
                }

                move.piece.position = move.to
                mutableHistory.replaceLast {
                    require(this is PentaMove.CanSetGrey) { "last move was not the expected move type: $this" }
                    withSetGrey(move)
                }
                selectedGrayPiece = null
                updatePiecesAtPos(move.to)
                updatePiecesAtPos(move.from)
            }
            is PentaMove.InitGame -> {
                initialized = true
                //TODO: create ClientPlayerState or ServerPlayerState ?
                players.clear()
                players += move.players.map { PlayerState(it, it) } // TODO: add players one by one
                turn = 0
                // TODO: set player count and names from `GameInit`
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
            }
            is PentaMove.Win -> {
                // TODO
                mutableHistory += move
                updateBoard()
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
    }

    /**
     * check if the moved piece is on a exit point
     */
    private fun postProcess(move: PentaMove.Move) {
        selectedPlayerPiece = null

        val targetField = move.to
        println("playerColor = ${move.playerPiece.pentaColor}")
        if (targetField is JointField) println("pentaColor = ${targetField.pentaColor}")
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
                println("selected gray piece: ${selectedGrayPiece?.id}")
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
                println("checking: ${nextField.id}")
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
                history.flatMap {
                    it.toSerializableList()
                }
            ) + "\n" +
                    history.joinToString("\n") {
                        it.asNotation()
                    }
        )
    }
}