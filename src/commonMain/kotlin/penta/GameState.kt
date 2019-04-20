package penta

import io.data2viz.geom.Point
import io.data2viz.math.deg
import penta.field.AbstractField
import penta.field.CornerField
import penta.field.JointField
import penta.figure.BlockerPiece
import penta.figure.BlockerType
import penta.figure.Piece
import penta.figure.PlayerPiece
import penta.math.length

data class GameState(
    // player ids
    val players: List<String>,
    // player id to team id
    val teams: Map<String, Int>
) {
    var updatePiece: (Piece) -> Unit = { piece -> }

    private var turn: Int = 0
    val currentPlayer: String
        get() = players[turn % players.count()]

    var selectedPlayerPiece: PlayerPiece? = null

    // TODO: add figure registry
    val figures: Array<Piece>
    private val positions: MutableMap<String, AbstractField?> = mutableMapOf()
    val figurePositions: Map<String, AbstractField?> get() = positions

    fun updatePiecePos(piece: Piece) {
        val field: AbstractField? = positions[piece.id]
        var pos: Point = field?.pos ?: run {
            val radius = when (piece) {
                is BlockerPiece -> {
                    when (piece.blockerType) {
                        BlockerType.GRAY -> PentaMath.inner_r * -0.25
                        BlockerType.BLACK -> throw IllegalStateException("black piece: $piece cannot be off the board")
                    }
                }
                is PlayerPiece -> PentaMath.inner_r * -0.5
                else -> throw NotImplementedError("unhandled piece type: ${piece::class}")
            }
            val angle = (piece.color.ordinal * -72.0).deg

            println("color: ${piece.color.ordinal}")

//            val radius = (PentaMath.inner_r / PentaMath.R_) * scale
            Point(
                radius * angle.cos,
                radius * angle.sin
            ).also {
                println(it)
            } / 2 + (Point(0.5, 0.5) * PentaMath.R_)
        }
        if(piece is PlayerPiece && field is CornerField) {
            // TODO: find all pieces on field and order them
            val pieceIds: List<String> = positions.filterValues { it == field }.keys
                .sorted()
            // TODO: find index of piece on field
            val pieceNumber = pieceIds.indexOf(piece.id).toDouble()
            val playerIndex = players.indexOf(piece.playerId).toDouble()
//            val angle = (((piece.color.ordinal * -72.0) + (playerIndex / players.size * 360.0)+360.0) % 360.0).deg
            val angle = (((field.pentaColor.ordinal * 72.0) + (pieceNumber / pieceIds.size * 360.0)+360.0) % 360.0).deg
//            val halfCircleWidth = (1.0 / PentaMath.R_) * scale / 2
            pos = Point(
                pos.x + (0.5) * angle.cos,
                pos.y + (0.5) * angle.sin
            )
        }
        if(piece is PlayerPiece && field == null) {
            // TODO: find all pieces on field and order them
            val playerPieces = positions.filterValues { it == field }.keys
                .map { id -> figures.find { it.id == id }!! }
                .filterIsInstance<PlayerPiece>()
                .filter { it.color == piece.color }
                .sortedBy { it.id }
            // TODO: find index of piece on field
            val pieceNumber = playerPieces.indexOf(piece).toDouble()
            val angle = (((piece.color.ordinal * -72.0) + (pieceNumber / playerPieces.size * 360.0)+360.0) % 360.0).deg
            pos = Point(
                pos.x + (0.4) * angle.cos,
                pos.y + (0.4) * angle.sin
            )
        }
        piece.pos = pos
        updatePiece(piece)
    }

    // init figures and positions
    init {
        val blacks = (0 until 5).map { i ->
            BlockerPiece(
                "b$i",
                BlockerType.BLACK,
                Point(0.0, 0.0),
                PentaMath.s / 2.5 / 2,
                PentaColor.values()[i]
            ). also {
                positions[it.id] = PentaBoard.j[i]
            }
        }
        val greys = (0 until 5).map { i ->
            BlockerPiece(
                "g$i",
                BlockerType.GRAY,
                Point(0.0, 0.0),
                PentaMath.s / 2.5/ 2,
                PentaColor.values()[i]
            ). also {
                positions[it.id] = null
            }
        }
        val players = (0 until players.size).flatMap { p ->
            (0 until 5).map { i ->
                PlayerPiece(
                    "p$p$i",
                    players[p],
                    Point(0.0, 0.0),
                    PentaMath.s / 1.5/ 2,
                    PentaColor.values()[i]
                ).also {
                    positions[it.id] = PentaBoard.c[i]
                }
            }
        }
        figures = (blacks + greys + players).toTypedArray()
        figures.forEach(::updatePiecePos)
    }


    fun doMove(move: Move) {
        // TODO: check if its the right turn
        // TODO: check if valid
        // TODO: record side effects
    }

    fun findPieceAtPos(mousePos: Point): Piece? = figures.find {
        (it.pos - mousePos).length < it.radius
    }

    fun clickPiece(clickedPiece: Piece) {
        // TODO: check turn
        if(selectedPlayerPiece == null && clickedPiece is PlayerPiece /*&& currentPlayer == clickedPiece.playerId*/) {
            println("selecting: $clickedPiece")
            selectedPlayerPiece = clickedPiece
            return
        }
        if(selectedPlayerPiece == clickedPiece && clickedPiece is PlayerPiece /*&& currentPlayer == clickedPiece.playerId*/) {
            println("deselecting: $clickedPiece")
            selectedPlayerPiece = null
            return
        }
        if(selectedPlayerPiece != null /*&& currentPlayer == selectedPlayerPiece!!.playerId*/) {
            val playerPiece = selectedPlayerPiece!!
            // TODO: doMove
            val sourcePos = positions[playerPiece.id] ?: run {
                println("piece if off the board already")
                return
            }
            val targetPos = positions[clickedPiece.id]
            if(targetPos == null) {
                println("$clickedPiece is not on the board")
//                selectedPlayerPiece = null
                return
            }
            println("swapping $sourcePos <-> $targetPos")


            // TODO: check if move is possible
            // swap pieces

            println("moving: ${playerPiece.id} -> $targetPos")
            positions[playerPiece.id] = targetPos
            // TODO movePiece(...) -> set position, update source pos, update target fields
            updatePiecesAtPos(sourcePos)
            updatePiecesAtPos(targetPos)
            selectedPlayerPiece = null

            when(clickedPiece) {
                is PlayerPiece -> {

                    if(sourcePos is JointField && sourcePos.pentaColor == clickedPiece.color)  {
                        // take piece off the board
                        positions[clickedPiece.id] = null
                        updatePiecesAtPos(targetPos)
//                        updatePiecePos(clickedPiece)
                        updatePiecesAtPos(null)

                        // other player moved this player into the joinfield, do we still let them set a grey
                        // this will overcomplicate things
                        // TODO: set gamestate to `MOVE_GREY`
                    } else {
                        // move normally
                        println("moving: ${clickedPiece.id} -> ${sourcePos.id}")
                        positions[clickedPiece.id] = sourcePos
                        updatePiecesAtPos(targetPos)
                        updatePiecesAtPos(sourcePos)
                    }
                    // TODO: also check if this is the matching JointField
                    // and leave board
                }
                is BlockerPiece -> {
                    when(clickedPiece.blockerType) {
                        BlockerType.GRAY ->  {
                            println("taking ${clickedPiece.id} off the board")
                            positions[clickedPiece.id] = null
                            updatePiecePos(clickedPiece)
                        }
                        BlockerType.BLACK -> {
                            // TODO: set gamestate to `MOVE_BLACK`
                            // TODO: implement moving black piece
//                            TODO("implement moving black piece")

                            // temporary
                            positions[clickedPiece.id] = sourcePos
                            updatePiecesAtPos(targetPos)
                            updatePiecesAtPos(sourcePos)

                        }
                    }
                }
            }

            // CHECK if targetPos is the target field for the clickedPiece

            if(targetPos is JointField && targetPos.pentaColor == playerPiece.color)  {
                // take piece off the board
                positions[playerPiece.id] = null
                updatePiecesAtPos(null)
//                updatePiecePos(playerPiece)
                // TODO: set gamestate to `MOVE_GREY`
            }

            PentaViz.viz.render()
            return
        }
        println("no action on click")
    }

    fun clickField(field: AbstractField) {
        if(selectedPlayerPiece != null /*&& currentPlayer == selectedPlayerPiece!!.playerId*/) {
            val playerPiece = selectedPlayerPiece!!

            val sourcePos = positions[playerPiece.id]!!
            val targetPos = field

            // check if targetField is empty
            if(positions.values.filter { it == targetPos }.isNotEmpty()) {
                println("target position not empty")
                return
            }

            // TODO: check if move is possible
            // swap pieces

            println("moving: ${playerPiece.id} -> $targetPos")
            positions[playerPiece.id] = targetPos
            // TODO movePiece(...) -> set position, update source pos, update target fields
            updatePiecesAtPos(sourcePos)
            updatePiecesAtPos(targetPos)
            selectedPlayerPiece = null


            if(targetPos is JointField && targetPos.pentaColor == playerPiece.color)  {
                positions[playerPiece.id] = null
//                updatePiecePos(playerPiece)
                updatePiecesAtPos(null)
                // TODO: set gamestate to `MOVE_GREY`
            }
            PentaViz.viz.render()
        }
    }

    private fun updatePiecesAtPos(field: AbstractField?) {
        positions.filterValues { it == field }.keys.map { id ->
            figures.find { it.id == id }!!
        }.forEach { piece ->
            updatePiecePos(piece)
        }
    }

    fun canMove(start: AbstractField, end: AbstractField) {
        
    }
}