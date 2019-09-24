package penta

import PentaBoard
import PentaMath
import io.data2viz.geom.Point
import penta.logic.field.AbstractField
import penta.logic.field.JointField
import penta.logic.Piece
import penta.util.exhaustive

open class BoardState(
    val players: List<String>,
    open val updateLogPanel: (String) -> Unit = {}
) {
    // player id to team id
    lateinit var teams: Map<String, Int>
        private set

    var figures: Array<Piece> = arrayOf()
        private set

    private val blacks: List<Piece.BlackBlocker>
    private val greys: List<Piece.GrayBlocker>
    private val positions: MutableMap<String, AbstractField?> = mutableMapOf()
    val figurePositions: Map<String, AbstractField?> get() = positions

    private val mutableHistory: MutableList<PentaMove> = mutableListOf()
    val history: List<PentaMove> = mutableHistory

    var turn: Int = 1
        private set

    var forceMoveNextPlayer: Boolean = false
        private set

    val currentPlayer: String
        get() = if (players.isNotEmpty()) players[turn % players.count()] else "nobody"

    var winner: String? = null
        private set

    var selectedPlayerPiece: Piece.Player? = null
        protected set
    var selectedBlackPiece: Piece.BlackBlocker? = null
        protected set
    var selectedGrayPiece: Piece.GrayBlocker? = null
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
                positions[it.id] = PentaBoard.j[i]
            }
        }
        greys = (0 until 5).map { i ->
            Piece.GrayBlocker(
                "g$i",
                Point(0.0, 0.0),
                PentaMath.s / 2.5,
                PentaColor.values()[i]
            ).also {
                positions[it.id] = null
            }
        }
        // TODO: set player count and names from `GameInit`
        val playerPieces = (0 until players.size).flatMap { p ->
            (0 until 5).map { i ->
                Piece.Player(
                    "p$p$i",
                    players[p],
                    Point(0.0, 0.0),
                    PentaMath.s / 2.3,
                    PentaColor.values()[i]
                ).also {
                    positions[it.id] = PentaBoard.c[i]
                }
            }
        }
        figures = arrayOf(*blacks.toTypedArray(), *greys.toTypedArray(), *playerPieces.toTypedArray())

        mutableHistory += PentaMove.InitGame(players)
    }

    inline fun requires(b: Boolean, error: () -> Unit) {
        if(!b) error()
    }

    fun processMove(move: PentaMove) {
        println("processing $move")
        when (move) {
            is PentaMove.MovePlayer -> {
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
                        }
                        is Piece.BlackBlocker -> {
                            selectedBlackPiece = pieceOnTarget
                            println("holding ${pieceOnTarget.id} for repositioning")
                        }
                        else -> {
                            TODO("signal illegal move")
                        }
                    }
                    // TODO: can unset when pieces float on pointer
//                    positions[pieceOnTarget.id] = null

                } else {
                    // no piece on target field
                }

                positions[move.playerPiece.id] = move.to

                postProcess(move)

                updatePiecesAtPos(move.to)
                updatePiecesAtPos(move.from)

                mutableHistory += move
            }
            is PentaMove.ForcedPlayerMove -> {
                val sourceField = move.from
                if(sourceField is JointField && move.playerPiece.pentaColor == sourceField.pentaColor ) {
                    postProcess(move)
                } else {
                    TODO("signal illegal move")
                }
                mutableHistory += move
            }
            is PentaMove.SwapOwnPiece -> {
                requires(canMove(move.from, move.to)) { TODO("signal illegal move") }
                requires(move.playerPiece.playerId == currentPlayer) { TODO("signal illegal move") }
                requires(positions[move.playerPiece.id] == move.from) { TODO("signal illegal move") }
                requires(move.otherPlayerPiece.playerId == currentPlayer) { TODO("signal illegal move") }
                requires(positions[move.otherPlayerPiece.id] == move.to) { TODO("signal illegal move") }

                positions[move.playerPiece.id] = move.to
                positions[move.otherPlayerPiece.id] = move.from
                updatePiecesAtPos(move.to)
                updatePiecesAtPos(move.from)

                postProcess(move)
                mutableHistory += move
            }
            is PentaMove.SwapHostilePieces -> {
                requires(canMove(move.from, move.to)) { TODO("signal illegal move") }
                requires(move.playerPiece.playerId == currentPlayer) { TODO("signal illegal move") }
                requires(positions[move.playerPiece.id] == move.from) { TODO("signal illegal move") }
                requires(move.otherPlayerPiece.playerId != currentPlayer) { TODO("signal illegal move") }
                requires(positions[move.otherPlayerPiece.id] == move.to) { TODO("signal illegal move") }

                positions[move.playerPiece.id] = move.to
                positions[move.otherPlayerPiece.id] = move.from
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
                    println("target position not empty")
                    TODO("signal illegal move")
                    return
                }
                requires(figurePositions[move.piece.id] == move.from) { TODO("signal illegal move") }

                positions[move.piece.id] = move.to
                val lastMove = mutableHistory.last()
                require(lastMove is PentaMove.CanSetBlack) { "last move was not the expected move type" }
                lastMove.setBlack = move
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
                requires(figurePositions[move.piece.id] == move.from) { TODO("signal illegal move") }
                requires(selectedGrayPiece == move.piece){ TODO("signal illegal move") }

                positions[move.piece.id] = move.to
                val lastMove = mutableHistory.last()
                require(lastMove is PentaMove.CanSetGrey) { "last move was not the expected move type" }
                lastMove.setGrey = move
                selectedGrayPiece = null
                updatePiecesAtPos(move.to)
                updatePiecesAtPos(move.from)
            }
            is PentaMove.InitGame -> {
//                players = move.players
//                turn = 0
//                // TODO: set player count and names from `GameInit`
//                val playerPieces = (0 until move.players.size).flatMap { p ->
//                    (0 until 5).map { i ->
//                        Piece.Player(
//                            "p$p$i",
//                            players[p],
//                            Point(0.0, 0.0),
//                            PentaMath.s / 2.3,
//                            PentaColor.values()[i]
//                        ).also {
//                            positions[it.id] = PentaBoard.c[i]
//                        }
//                    }
//                }
//                figures = arrayOf<Piece>(*blacks.toTypedArray(), *greys.toTypedArray(), *playerPieces.toTypedArray())
//                //(blacks + greys + playerPieces).toTypedArray<Piece>()
//                history += move
            }
            is PentaMove.Win -> {
                // TODO
            }
        }.exhaustive

        checkWin()

        if (selectedBlackPiece == null && selectedGrayPiece == null && !selectingGrayPiece) {
            turn += 1
        }
//        if(forceMoveNextPlayer) {
            forceMovePlayerPiece(currentPlayer)
//        }

        updateBoard()
    }

    private fun postProcess(move: PentaMove.Move) {
        selectedPlayerPiece = null

        val targetField = move.to
        if (targetField is JointField && targetField.pentaColor == move.playerPiece.pentaColor) {
            // take piece off the board
            positions[move.playerPiece.id] = null
            updatePiecesAtPos(null)

            // set gamestate to `MOVE_GREY`
            selectedGrayPiece = positions.filterValues { it == null }
                .keys.map { id -> figures.find { it.id == id } }
                .filterIsInstance<Piece.GrayBlocker>()
                .firstOrNull()
            if (selectedGrayPiece == null) {
                selectingGrayPiece = true
            }
        }
    }

    private fun forceMovePlayerPiece(player: String) {
        if(selectingGrayPiece || selectingGrayPiece) return
        val playerPieces = figures.filterIsInstance<Piece.Player>().filter { it.playerId == player }
        for (playerPiece in playerPieces) {
            val field = positions[playerPiece.id] as? JointField ?: continue
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
        if (winner != null || turn % players.size != 0) return
        val winners = players.filter { player ->
            val playerPieces = figures.filterIsInstance<Piece.Player>().filter { it.playerId == player }
            val offBoardPieces = playerPieces.filter { positions[it.id] == null }

            offBoardPieces.size >= 3
        }
        if(winners.isNotEmpty()) {
            winner = winners.joinToString(", ")
            processMove(PentaMove.Win(winner!!))
        }
//        val playerPieces = figures.filterIsInstance<Piece.Player>().groupBy { it.playerId }
//        val offBoardPieces = playerPieces.filter { positions[it.id] == null }
//        if (offBoardPieces.size >= 1) {
//            println("pieces: ${offBoardPieces.joinToString { it.id }} are off the board")
//            println("player $player won")
//            winner = player
//            mutableHistory += PentaMove.Win(player = player)
//            updateLogPanel(mutableHistory.flatMap { it.toSerializableList() }.joinToString("\n"))
//        }
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

    open protected fun updateAllPieces() {
        figures.forEach { piece ->
            updatePiecePos(piece)
        }
    }
    protected open fun updatePiecePos(piece: Piece) {}
    protected open fun updatePiecesAtPos(field: AbstractField?) {}
    protected open fun updateBoard() {}
}