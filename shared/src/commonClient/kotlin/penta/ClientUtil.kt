package penta

import com.github.nwillc.ksvg.elements.SVG
import io.data2viz.color.Color
import io.data2viz.color.Colors
import io.data2viz.geom.Point
import io.data2viz.math.Angle
import io.data2viz.math.deg
import io.data2viz.viz.PathNode
import mu.KotlinLogging
import penta.logic.Piece
import penta.logic.field.AbstractField
import penta.logic.field.StartField
import penta.redux_rewrite.BoardState
import kotlin.math.pow
import kotlin.math.sqrt

fun canClickPiece(clickedPiece: Piece, boardState: BoardState): Boolean {
    with(boardState) {
        if (winner != null) {
            return false
        }
        if (positions[clickedPiece.id] == null) {
            return false
        }
        // TODO: have multiplayer state in store
        when (val state: ConnectionState = ConnectionState.Disconnected()/*penta.client.PentaViz.multiplayerState.value*/) {
            is ConnectionState.HasGameSession -> {
                if (currentPlayer.id != state.userId) {
                    return false
                }
            }
        }
        if (
        // make sure you are not selecting black or gray
            selectedGrayPiece == null && selectedBlackPiece == null && !selectingGrayPiece
            && clickedPiece is Piece.Player && currentPlayer.id == clickedPiece.playerId
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

        if (selectedPlayerPiece != null && currentPlayer.id == selectedPlayerPiece!!.playerId) {
            val playerPiece = selectedPlayerPiece!!
            val sourcePos = positions[playerPiece.id] ?: run {
                return false
            }
            val targetPos = positions[clickedPiece.id] ?: return false
            if (sourcePos == targetPos) {
                return false
            }
            return true
        }
    }
    return false
}

fun SVG.drawFigure(figureId: String, center: Point, radius: Double, color: Color, selected: Boolean) {
    drawPlayer(
        figureId, center, radius, color, null, selected, false
    )
}

fun SVG.drawPlayer(figureId: String, center: Point, radius: Double, piece: Piece.Player, selected: Boolean, highlight: Boolean, clickable: Boolean) {
    val color = when {
        selected -> piece.color.brighten(0.5)
        else -> piece.color
    }
    drawPlayer(
        figureId = figureId,
        center = center,
        radius = radius,
        color = color,
        pieceId = if(clickable) piece.id else null,
        selected = selected,
        highlight = highlight
    )
}

fun SVG.drawPlayer(
    figureId: String, center: Point, radius: Double, color: Color,
    pieceId: String?, selected: Boolean, highlight: Boolean
) {
    fun point(angle: Angle, radius: Double, center: Point): Point {
        return Point(angle.cos * radius, angle.sin * radius) + center
    }

    fun angles(n: Int, start: Angle = 0.deg): List<Angle> {
        val step = 360.deg / n

        return (0..n).map { i ->
            (start + (step * i))
        }
    }

    val lineWidth = when {
        selected -> "3.0"
        highlight -> "4.0"
        else -> "1.0"
    }
    val fillColor = when {
        selected -> color.brighten(1.0)
        else -> color
    }
    val strokeColor = when {
        highlight -> Colors.Web.gray
        else -> Colors.Web.black
    }

    when (figureId) {
        "square" -> {
            val points = angles(4, 0.deg).map { angle ->
                point(angle, radius, center)
            }

            polygon {
                if(pieceId != null) {
                    id = pieceId
                }
                this.points = points.joinToString(" ") { "${it.x},${it.y}" }

                fill = fillColor.rgbHex
                strokeWidth = lineWidth
                stroke = strokeColor.rgbHex
            }
        }
        "triangle" -> {
            val points = angles(3, -90.deg).map { angle ->
                point(angle, radius, center)
            }
            polygon {
                if(pieceId != null) {
                    id = pieceId
                }
                this.points = points.joinToString(" ") { "${it.x},${it.y}" }

                fill = fillColor.rgbHex
                strokeWidth = lineWidth
                stroke = strokeColor.rgbHex
            }
        }
        "cross" -> {
            val width = 15

            val p1 = point((45 - width).deg, radius, center)
            val p2 = point((45 + width).deg, radius, center)

            val c = sqrt((p2.x - p1.x).pow(2) + (p2.y - p1.y).pow(2))

            val a = c / sqrt(2.0)

            val points = listOf(
                point((45 - width).deg, radius, center),
                point((45 + width).deg, radius, center),
                point((90).deg, a, center),
                point((135 - width).deg, radius, center),
                point((135 + width).deg, radius, center),
                point((180).deg, a, center),
                point((45 + 180 - width).deg, radius, center),
                point((45 + 180 + width).deg, radius, center),
                point((270).deg, a, center),
                point((135 + 180 - width).deg, radius, center),
                point((135 + 180 + width).deg, radius, center),
                point((360).deg, a, center)
            )

            polygon {
                if(pieceId != null) {
                    id = pieceId
                }
                this.points = points.joinToString(" ") { "${it.x},${it.y}" }

                fill = fillColor.rgbHex
                strokeWidth = lineWidth
                stroke = strokeColor.rgbHex
            }
        }
        "circle" -> {
            circle {
                if(pieceId != null) {
                    id = pieceId
                }
                cx = "${center.x}"
                cy = "${center.y}"
                r = "${radius * 0.8}"
                fill = color.rgbHex
                strokeWidth = lineWidth
                stroke = strokeColor.rgbHex
            }
        }
        else -> throw IllegalStateException("illegal figureId: '$figureId'")
    }
}


fun cornerPoint(index: Int, angleDelta: Angle = 0.deg, radius: Double = PentaMath.R_): Point {
    val angle = (-45 + (index) * 90).deg + angleDelta

    return Point(
        radius * angle.cos,
        radius * angle.sin
    ) / 2 + (Point(0.5, 0.5) * PentaMath.R_)
}

fun calculatePiecePos(piece: Piece, field: AbstractField?, boardState: BoardState) = with(boardState) {
    val logger = KotlinLogging.logger {}
    var pos: Point = field?.pos ?: run {
        val radius = when (piece) {
            is Piece.GrayBlocker -> {
                logger.debug {"piece: ${piece.id}"}
                logger.debug {"selected: ${selectedGrayPiece?.id}"}
                if (selectedGrayPiece == piece) {
                    val index = players.indexOf(currentPlayer)
                    val pos = cornerPoint(index, 10.deg, radius = (PentaMath.R_ + (3 * PentaMath.s)))
                    return@run pos
                }
                PentaMath.inner_r * -0.2
            }
            is Piece.BlackBlocker -> {
                if (selectedBlackPiece == piece) {
                    val index = players.indexOf(currentPlayer)
                    val pos = cornerPoint(index, (-10).deg, radius = (PentaMath.R_ + (3 * PentaMath.s)))
                    logger.debug {"cornerPos: $pos" }
                    return@run pos
                }
                throw IllegalStateException("black piece: $piece cannot be off the board")
            }
            is Piece.Player -> PentaMath.inner_r * -0.5
//                else -> throw NotImplementedError("unhandled piece type: ${piece::class}")
        }
        val angle = (piece.pentaColor.ordinal * -72.0).deg

        logger.debug { "pentaColor: ${piece.pentaColor.ordinal}" }

        Point(
            radius * angle.cos,
            radius * angle.sin
        ) / 2 + (Point(0.5, 0.5) * PentaMath.R_)
    }
    if (piece is Piece.Player && field is StartField) {
        // find all pieces on field and order them
        val pieceIds: List<String> = positions.filterValues { it == field }.keys
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
        val playerPieces = positions.filterValues { it == field }.keys
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
    pos
}

fun PathNode.drawFigure(figureId: String, center: Point, radius: Double) {
    clearPath()

    fun point(angle: Angle, radius: Double, center: Point = Point(0.0, 0.0)): Point {
        return Point(angle.cos * radius, angle.sin * radius) + center
    }

    fun angles(n: Int, start: Angle = 0.deg): List<Angle> {
        val step = 360.deg / n

        return (0..n).map { i ->
            (start + (step * i))
        }
    }

    when (figureId) {
        "square" -> {
            val points = angles(4, 0.deg).map { angle ->
                point(angle, radius, center)
            }
            points.forEachIndexed { index, it ->
                if (index == 0) {
                    moveTo(it.x, it.y)
                } else {
                    lineTo(it.x, it.y)
                }
            }
        }
        "triangle" -> {
            val points = angles(3, -90.deg).map { angle ->
                point(angle, radius, center)
            }
            points.forEachIndexed { index, it ->
                if (index == 0) {
                    moveTo(it.x, it.y)
                } else {
                    lineTo(it.x, it.y)
                }
            }
        }
        "cross" -> {

            val width = 15

            val p1 = point((45 - width).deg, radius, center)
            val p2 = point((45 + width).deg, radius, center)

            val c = sqrt((p2.x - p1.x).pow(2) + (p2.y - p1.y).pow(2))

            val a = c / sqrt(2.0)

            val points = listOf(
                point((45 - width).deg, radius, center),
                point((45 + width).deg, radius, center),
                point((90).deg, a, center),
                point((135 - width).deg, radius, center),
                point((135 + width).deg, radius, center),
                point((180).deg, a, center),
                point((45 + 180 - width).deg, radius, center),
                point((45 + 180 + width).deg, radius, center),
                point((270).deg, a, center),
                point((135 + 180 - width).deg, radius, center),
                point((135 + 180 + width).deg, radius, center),
                point((360).deg, a, center)
            )
            points.forEachIndexed { index, it ->
                if (index == 0) {
                    moveTo(it.x, it.y)
                } else {
                    lineTo(it.x, it.y)
                }
            }
        }
        "circle" -> {
            arc(center.x, center.y, radius, 0.0, 180.0, false)
        }
        else -> throw IllegalStateException("illegal figureId: ''")
    }
    closePath()
}