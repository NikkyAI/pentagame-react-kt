package penta

import PentaMath
import PentaViz
import io.data2viz.geom.Point
import io.data2viz.math.deg
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import kotlinx.serialization.modules.SerializersModule
import penta.logic.Piece
import penta.logic.field.AbstractField
import penta.logic.field.CornerField
import penta.util.length

class ClientGameState(
//    // player ids
    players: List<String>,
//    // player id to team id
//    val teams: Map<String, Int>,
    override val updateLogPanel: (String) -> Unit = {}
) : BoardState() {
    var updatePiece: (Piece) -> Unit = { piece -> }

    init {
        figures.forEach(::updatePiecePos)
    }

    fun initialize(players: List<String>) {
        println("initializing with $players")
        processMove(PentaMove.InitGame(players))
    }

//    var turn: Int = 0
//        private set
//
//    var winner: String? = null
//
//    var forceMoveNextPlayer: Boolean = false
//
//    val currentPlayer: String
//        get() = if (players.isNotEmpty()) players[turn % players.count()] else throw IllegalStateException("player list is empty")
//
//    var selectedPlayerPiece: Piece.Player? = null
//    var selectedBlackPiece: Piece.BlackBlocker? = null
//    var selectedGrayPiece: Piece.GrayBlocker? = null
//
//    /**
//     * true when no gray pieces are in the middle and one from the board can be selected
//     */
//    var selectingGrayPiece: Boolean = false
//
//    // TODO: add figure registry
//    val figures: Array<Piece>
//    private val positions: MutableMap<String, AbstractField?> = mutableMapOf()
//    val figurePositions: Map<String, AbstractField?> get() = positions


    // TODO: move to client
    // TODO: not common code
    override fun updatePiecePos(piece: Piece) {
        val field: AbstractField? = figurePositions[piece.id]
        var pos: Point = field?.pos ?: run {
            val radius = when (piece) {
                is Piece.GrayBlocker -> PentaMath.inner_r * -0.2
                is Piece.BlackBlocker -> throw IllegalStateException("black piece: $piece cannot be off the board")
                is Piece.Player -> PentaMath.inner_r * -0.5
                else -> throw NotImplementedError("unhandled piece type: ${piece::class}")
            }
            val angle = (piece.pentaColor.ordinal * -72.0).deg

            println("pentaColor: ${piece.pentaColor.ordinal}")

//            val radius = (PentaMath.inner_r / PentaMath.R_) * scale
            Point(
                radius * angle.cos,
                radius * angle.sin
            ).also {
                println(it)
            } / 2 + (Point(0.5, 0.5) * PentaMath.R_)
        }
        if (piece is Piece.Player && field is CornerField) {
            // find all pieces on field and order them
            val pieceIds: List<String> = figurePositions.filterValues { it == field }.keys
                .sorted()
            // find index of piece on field
            val pieceNumber = pieceIds.indexOf(piece.id).toDouble()
            val angle =
                (((field.pentaColor.ordinal * -72.0) + (pieceNumber / pieceIds.size * 360.0) + 360.0) % 360.0).deg
            pos = Point(
                pos.x + (0.55) * angle.cos,
                pos.y + (0.55) * angle.sin
            )
        }
        if (piece is Piece.Player && field == null) {
            // find all pieces on field and order them
            val playerPieces = figurePositions.filterValues { it == field }.keys
                .map { id -> figures.find { it.id == id }!! }
                .filterIsInstance<Piece.Player>()
                .filter { it.pentaColor == piece.pentaColor }
                .sortedBy { it.id }
            // find index of piece on field
            val pieceNumber = playerPieces.indexOf(piece).toDouble()
            val angle =
                (((piece.pentaColor.ordinal * -72.0) + (pieceNumber / playerPieces.size * 360.0) + 360.0 + 180.0) % 360.0).deg
            pos = Point(
                pos.x + (0.55) * angle.cos,
                pos.y + (0.55) * angle.sin
            )
        }
        piece.pos = pos
        updatePiece(piece)
    }

    // TODO: clientside
    fun findPiecesAtPos(mousePos: Point) = figures.filter {
        (it.pos - mousePos).length < it.radius
    }

    fun canClickPiece(clickedPiece: Piece): Boolean {
        if (winner != null) {
            return false
        }
        if (figurePositions[clickedPiece.id] == null) {
            return false
        }
        if (
        // make sure you are not selecting black or gray
            selectedGrayPiece == null && selectedBlackPiece == null && !selectingGrayPiece
            && clickedPiece is Piece.Player && currentPlayer == clickedPiece.playerId
        ) {
            if (selectedPlayerPiece == null) {
                return true
            }
            if (selectedPlayerPiece == clickedPiece) {
                return true
            }
        }

        if (selectingGrayPiece
            && selectedPlayerPiece == null
            && clickedPiece is Piece.GrayBlocker
        ) {
            return true
        }

        if (selectedPlayerPiece != null && currentPlayer == selectedPlayerPiece!!.playerId) {
            val playerPiece = selectedPlayerPiece!!
            val sourcePos = figurePositions[playerPiece.id] ?: run {
                return false
            }
            val targetPos = figurePositions[clickedPiece.id] ?: return false
            if (sourcePos == targetPos) {
                return false
            }
            return true
        }
        return false
    }

    // TODO: clientside
    /**
     * click on a piece
     * @param clickedPiece game piece that was clicked on
     */
    fun clickPiece(clickedPiece: Piece) {
        // TODO: check turn
        println("currentPlayer: $currentPlayer")
        println("selected player piece: $selectedPlayerPiece")
        println("selected black piece: $selectedBlackPiece")
        println("selected gray piece: $selectedGrayPiece")

        if (!canClickPiece(clickedPiece)) return

        if (figurePositions[clickedPiece.id] == null) {
            println("cannot click piece off the board")
            return
        }
        if (
        // make sure you are not selecting black or gray
            selectedGrayPiece == null && selectedBlackPiece == null && !selectingGrayPiece
            && clickedPiece is Piece.Player && currentPlayer == clickedPiece.playerId
        ) {
            if (selectedPlayerPiece == null) {
                println("selecting: $clickedPiece")
                selectedPlayerPiece = clickedPiece
                PentaViz.updateBoard()
                return
            }
            if (selectedPlayerPiece == clickedPiece) {
                println("deselecting: $clickedPiece")
                selectedPlayerPiece = null
                PentaViz.updateBoard()
                return
            }
        }

        if (selectingGrayPiece
            && selectedPlayerPiece == null
            && clickedPiece is Piece.GrayBlocker
        ) {
            println("selecting: $clickedPiece")
            selectedGrayPiece = clickedPiece
            selectingGrayPiece = false
            PentaViz.updateBoard()
            return
        }
        if (selectedPlayerPiece != null && currentPlayer == selectedPlayerPiece!!.playerId) {
            val playerPiece = selectedPlayerPiece!!
            val sourceField = figurePositions[playerPiece.id] ?: run {
                println("piece if off the board already")
                return
            }
            val targetField = figurePositions[clickedPiece.id]
            if (targetField == null) {
                println("$clickedPiece is not on the board")
//                selectedPlayerPiece = null
                return
            }
            if (sourceField == targetField) {
                println("cannot move piece onto the same field as before")
                return
            }

            if (!canMove(sourceField, targetField)) {
                println("can not find path")
                return
            }

            println("moving: ${playerPiece.id} -> $targetField")

            val move: PentaMove = when (clickedPiece) {
                is Piece.Player -> {
                    if (playerPiece.playerId == clickedPiece.playerId) {
                        PentaMove.SwapOwnPiece(
                            playerPiece = playerPiece, otherPlayerPiece = clickedPiece,
                            from = sourceField, to = targetField
                        )
                    } else {
                        // TODO   if (player is in your team) {
                        PentaMove.SwapHostilePieces(
                            playerPiece = playerPiece, otherPlayerPiece = clickedPiece,
                            from = sourceField, to = targetField
                        )
                    }
                }
                is Piece.GrayBlocker -> {
                    PentaMove.MovePlayer(
                        playerPiece = playerPiece, from = sourceField, to = targetField
                    )
                }
                is Piece.BlackBlocker -> {
                    PentaMove.MovePlayer(
                        playerPiece = playerPiece, from = sourceField, to = targetField
                    )
                }
            }
            processMove(move)

            return
        }
        println("no action on click")
    }

    override fun updateBoard() {
        super.updateBoard()
        PentaViz.updateBoard()
    }

    // TODO: clientside
    fun canClickField(targetField: AbstractField): Boolean {
        if (winner != null) {
            return false
        }
        if (
            (selectedPlayerPiece == null && selectedGrayPiece == null && selectedBlackPiece == null)
            && figurePositions.none { (k, v) -> v == targetField }
        ) {
            return false
        }
        when {
            selectedPlayerPiece != null && currentPlayer == selectedPlayerPiece!!.playerId -> {
                val playerPiece = selectedPlayerPiece!!

                val sourcePos = figurePositions[playerPiece.id]!!
                if (sourcePos == targetField) {
                    return false
                }

                // check if targetField is empty
                if (figurePositions.values.any { it == targetField }) {
                    val pieces = figurePositions.filterValues { it == targetField }.keys
                        .map { id ->
                            figures.find { it.id == id }
                        }
                    pieces.firstOrNull() ?: return false
                    return true
                }
            }
            selectedBlackPiece != null -> {
                if (figurePositions.values.any { it == targetField }) {
//                    println("target position not empty")
                    return false
                }
            }
            selectedGrayPiece != null -> {
                if (figurePositions.values.any { it == targetField }) {
//                    println("target position not empty")
                    return false
                }
            }
            selectedPlayerPiece == null && selectedBlackPiece == null && selectedGrayPiece == null -> {
                // do not allow clicking on field when selecting piece
                return false
            }
        }
        return true
    }

    // TODO: clientside
    fun clickField(targetField: AbstractField) {
        println("currentPlayer: $currentPlayer")
        println("selected player piece: $selectedPlayerPiece")
        println("selected black piece: $selectedBlackPiece")
        println("selected gray piece: $selectedGrayPiece")
        if (!canClickField(targetField)) return
        val move = when {
            selectedPlayerPiece != null && currentPlayer == selectedPlayerPiece!!.playerId -> {
                val playerPiece = selectedPlayerPiece!!

                val sourceField = figurePositions[playerPiece.id]!!
                if (sourceField == targetField) {
                    println("cannot move piece onto the same field as before")
                    return
                }

                // check if targetField is empty
                if (figurePositions.values.any { it == targetField }) {
                    println("target position not empty")
                    // TODO: if there is only one piece on the field, click that piece instead ?
                    val pieces = figurePositions.filterValues { it == targetField }.keys
                        .map { id ->
                            figures.find { it.id == id }
                        }
                    if (pieces.size == 1) {
                        val piece = pieces.firstOrNull() ?: return
                        clickPiece(piece)
                    }
                    return
                }

                if (!canMove(sourceField, targetField)) {
                    println("can not find path")
                    return
                }

                println("moving: ${playerPiece.id} -> $targetField")

                PentaMove.MovePlayer(
                    playerPiece = playerPiece, from = sourceField, to = targetField
                )
            }
            selectedBlackPiece != null -> {
                val blackPiece = selectedBlackPiece!!

                if (figurePositions.values.any { it == targetField }) {
                    println("target position not empty")
                    return
                }
                val lastMove = history.last() as PentaMove.Move
                if (lastMove !is PentaMove.CanSetBlack) {
                    println("last move cannot set black")
                    return
                }

                PentaMove.SetBlack(
                    piece = blackPiece, from = lastMove.to, to = targetField
                )
            }
            selectedGrayPiece != null -> {
                val grayPiece = selectedGrayPiece!!

                if (figurePositions.values.any { it == targetField }) {
                    println("target position not empty")
                    return
                }
                val originPos = figurePositions[grayPiece.id]

                PentaMove.SetGrey(
                    piece = grayPiece, from = originPos, to = targetField
                )
            }
            else -> {
                TODO("handle else case")
            }
        }
        processMove(move)
    }

    override fun resetBoard() {
        PentaViz.gameState = this
        PentaViz.resetBoard()
    }

    // TODO: clientside
    override fun updateAllPieces() {
        figures.forEach { piece ->
            updatePiecePos(piece)
        }
    }

    // TODO: clientside
    override fun updatePiecesAtPos(field: AbstractField?) {
        figurePositions.filterValues { it == field }.keys.map { id ->
            figures.find { it.id == id }!!
        }.forEach { piece ->
            updatePiecePos(piece)
        }
    }
}