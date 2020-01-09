package penta

import io.data2viz.geom.Point
import io.data2viz.math.Angle
import io.data2viz.math.deg
import io.data2viz.viz.PathNode
import penta.logic.Piece
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



fun PathNode.drawPlayer(figureId: String, center: Point, radius: Double) {
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