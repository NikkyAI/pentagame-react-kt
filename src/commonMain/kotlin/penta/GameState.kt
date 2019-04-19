package penta

import io.data2viz.geom.Point
import io.data2viz.math.deg
import penta.field.AbstractField
import penta.field.CornerField
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
    var updatePiece: (Piece, Piece?) -> Unit = { piece, highlightedPiece ->

    }

    private var turn: Int = 0
    val getCurrentPlayer: String
        get() = players[turn % players.count()]

    // TODO: add figure registry
    val figures: Array<Piece>
    private val positions: MutableMap<Piece, AbstractField?> = mutableMapOf()
    val figurePositions: Map<Piece, AbstractField?> = positions

    fun updatePiecePos(piece: Piece) {
        val field = positions[piece]
        var pos: Point = field?.pos ?: run {
            val radius = when (piece) {
                is BlockerPiece -> {
                    when (piece.blockerType) {
                        BlockerType.GRAY -> PentaMath.inner_r * -0.3
                        BlockerType.BLACK -> throw IllegalStateException("black piece: $piece cannot be off the board")
                    }
                }
                is PlayerPiece -> PentaMath.inner_r * -0.7
                else -> throw NotImplementedError("unhandled piece type: ${piece::class}")
            }
            val angle = (piece.color.ordinal * -72.0).deg

            println("color: ${piece.color.ordinal}")
            val jointPiece = PentaBoard.j[piece.color.ordinal]
            println("jointPiece: ${jointPiece}")

//            val radius = (PentaMath.inner_r / PentaMath.R_) * scale
            Point(
                radius * angle.cos,
                radius * angle.sin
            ).also {
                println(it)
            } / 2 + (Point(0.5, 0.5) * PentaMath.R_)
        }
        if(piece is PlayerPiece&& field is CornerField) {
            val playerIndex = players.indexOf(piece.playerId).toDouble()
            val angle = (((piece.color.ordinal * -72.0) + (playerIndex / players.size * 360.0)+360.0) % 360.0).deg
//            val halfCircleWidth = (1.0 / PentaMath.R_) * scale / 2
            pos = Point(
                pos.x + (0.5) * angle.cos,
                pos.y + (0.5) * angle.sin
            )
        }
        piece.pos = pos
        updatePiece(piece, null)
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
                positions[it] = PentaBoard.j[i]
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
                positions[it] = null
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
                    positions[it] = PentaBoard.c[i]
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
}