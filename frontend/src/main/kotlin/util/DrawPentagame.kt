package util

import com.github.nwillc.ksvg.elements.SVG
import debug
import io.data2viz.color.Colors
import io.data2viz.color.col
import io.data2viz.math.deg
import penta.BoardState
import penta.ConnectionState
import penta.PentaColors
import penta.PlayerState
import penta.UserInfo
import penta.calculatePiecePos
import penta.cornerPoint
import penta.drawFigure
import penta.drawPlayer
import penta.logic.Field
import penta.logic.Piece

data class DrawProps(
    val boardState: BoardState,
    val playingUsers: Map<PlayerState, UserInfo>,
    val figures: Map<Field, Piece>,
    val canMoveToField: Map<Field, Boolean>,
    val drawCorners: Boolean = true,
    val interactive: Boolean = true,
    val isYourTurn: Boolean? = null
//if (svgProps.connection is ConnectionState.ConnectedToGame) {
//        svgProps.boardState.currentPlayer.id == svgProps.connection.userId
//    } else false
)

fun SVG.drawPentagame(scale: Int, boardState: BoardState, connection: ConnectionState, playingUsers: Map<PlayerState, UserInfo>) {
//        val lineWidth = (PentaMath.s / 5) / PentaMath.R_ * scale
    val lineWidth = 0.1 / PentaMath.R_ * scale

    console.debug("lineWidth: $lineWidth")
//        console.log("thinLineWidth: $thinLineWidth")

//        style {
//            body = """
//             svg .black-stroke { stroke: black; stroke-width: 2; }
//            """.trimIndent()
//        }
//    val boardState = svgProps.boardState

    val isYourTurn = if (connection is ConnectionState.ConnectedToGame) {
        boardState.currentPlayer.id == connection.userId
    } else true // in local game it is always your turn
    val selectedPieceIsCurrentPlayer = boardState.selectedPlayerPiece?.player == boardState.currentPlayer
    val selectedPlayerPieceField = if (selectedPieceIsCurrentPlayer) {
        boardState.positions[boardState.selectedPlayerPiece?.id] ?: run {
            throw IllegalStateException("cannot find field for ${boardState.selectedPlayerPiece}")
        }
    } else null
    val hasBlockerSelected =
        isYourTurn && (boardState.selectedBlackPiece != null || boardState.selectedGrayPiece != null)

    // background circle
    circle {
        cx = "${0.5 * scale}"
        cy = "${0.5 * scale}"
        r = "${(PentaMath.R_ / PentaMath.R_ * scale) / 2}"
        fill = PentaColors.BACKGROUND.rgbHex
//            fill = Colors.Web.blue.rgbHex
    }

    // outer ring
    circle {
        cx = "${0.5 * scale}"
        cy = "${0.5 * scale}"
        r = "${(PentaMath.r / PentaMath.R_ * scale) / 2}"
        stroke = PentaColors.FOREGROUND.rgbHex
        strokeWidth = "$lineWidth"
        fill = PentaColors.BACKGROUND.rgbHex
    }

    val emptyFields = PentaBoard.fields - boardState.positions.mapNotNull { (_, field) -> field }

    PentaBoard.fields.forEach { field ->
        val canMoveToField = if (selectedPlayerPieceField != null) {
            boardState.canMove(selectedPlayerPieceField, field)
        } else false

        val canPlaceBlocker = hasBlockerSelected && field in emptyFields

        val scaledPos = field.pos / PentaMath.R_ * scale
        val radius = (field.radius / PentaMath.R_ * scale)
        when (field) {
            is Field.Start -> {
                circle {
                    // TODO: add path checking, only make reachable pieces clickable
                    if (isYourTurn && (selectedPieceIsCurrentPlayer || canPlaceBlocker)) {
                        cssClass = field.id
                    }
                    cx = "${scaledPos.x}"
                    cy = "${scaledPos.y}"
                    r = "${radius - lineWidth}"
                    stroke = field.color.rgbHex
                    strokeWidth = lineWidth.toString()
                    fill = if (canMoveToField || canPlaceBlocker) {
                        PentaColors.FOREGROUND.brighten()
                    } else {
                        PentaColors.FOREGROUND
                    }.rgbHex
                }
            }
            is Field.Goal -> {
                circle {
                    // TODO: add path checking, only make reachable pieces clickable
                    if (isYourTurn && (selectedPieceIsCurrentPlayer || canPlaceBlocker)) {
                        cssClass = field.id
                    }
                    cx = "${scaledPos.x}"
                    cy = "${scaledPos.y}"
                    r = "${radius - (lineWidth / 2)}"
                    fill = if (canMoveToField || canPlaceBlocker) {
                        field.color.brighten()
                    } else {
                        field.color
                    }.rgbHex
//                    strokeWidth = "${scaledLineWidth / 10}"
//                    stroke = "#28292b"
                }
            }
            is Field.ConnectionField -> {
                circle {
                    // TODO: add path checking, only make reachable pieces clickable
                    if (isYourTurn && (selectedPieceIsCurrentPlayer || canPlaceBlocker)) {
                        cssClass = field.id
                    }
                    cx = "${scaledPos.x}"
                    cy = "${scaledPos.y}"
                    r = "${radius - (lineWidth / 10)}"
                    fill = if (canMoveToField || canPlaceBlocker) {
                        field.color.brighten()
                    } else {
                        field.color
                    }.rgbHex
                    strokeWidth = "${lineWidth / 2}"
                    stroke = PentaColors.BACKGROUND.rgbHex
                    asDynamic().onclick = { it: Any -> console.log("clicked $it ") }
                }
            }
        }
    }

    // add corner fields
    boardState.gameType.players.forEachIndexed { i, player ->
        console.debug("init face $player")
        val centerPos = cornerPoint(
            index = i,
            angleDelta = 0.deg,
            radius = (PentaMath.R_ + (3 * PentaMath.s))
        ) / PentaMath.R_ * scale
        val blackPos = cornerPoint(
            index = i,
            angleDelta = -10.deg,
            radius = (PentaMath.R_ + (3 * PentaMath.s))
        ) / PentaMath.R_ * scale
        val greyPos = cornerPoint(
            index = i,
            angleDelta = 10.deg,
            radius = (PentaMath.R_ + (3 * PentaMath.s))
        ) / PentaMath.R_ * scale
        val blockerRadius = Piece.Blocker.RADIUS / PentaMath.R_ * scale

        val pieceRadius = (PentaMath.s / PentaMath.R_ * scale) / 2

        // draw figure background
        val bgColor = Colors.Web.lightgrey.brighten(0.5).rgbHex

        circle {
            cx = "${centerPos.x}"
            cy = "${centerPos.y}"
            r = "${pieceRadius * 2}"
            fill = bgColor

            // draw ring around current player
            if (player.id == boardState.currentPlayer.id) {
                stroke = 0.col.rgbHex
                strokeWidth = "3.0"
            }
        }
        // draw black blocker background
        circle {
            cx = "${blackPos.x}"
            cy = "${blackPos.y}"
            r = "${blockerRadius * 2}"
            stroke = Colors.Web.grey.rgbHex
            strokeWidth = "2.0"
            fill = bgColor
        }
        // draw grey blocker background
        circle {
            cx = "${greyPos.x}"
            cy = "${greyPos.y}"
            r = "${blockerRadius * 2}"
            stroke = Colors.Web.grey.rgbHex
            strokeWidth = "2.0"
            fill = bgColor
        }

        val playinguser = playingUsers[player]
        if (playinguser != null) {
            // draw player face
            drawFigure(
                figureId = playinguser.figureId,
                center = centerPos,
                radius = pieceRadius,
                color = Colors.Web.black,
                selected = boardState.currentPlayer.id == player.id
            )
        } else {
            console.warn("no player $player in ${playingUsers}")
            //TODO: render placeholder figures
        }

        // show gray slot when selecting gray
        if (
            boardState.currentPlayer.id == player.id &&
            boardState.selectingGrayPiece
        ) {
            circle {
                cx = "${greyPos.x}"
                cy = "${greyPos.y}"
                r = "$blockerRadius"
                stroke = Colors.Web.grey.rgbHex
                strokeWidth = "2.0"
                fill = Colors.Web.white.rgbHex
            }
        }
    }

    boardState.figures.forEach { piece ->
        val pos = calculatePiecePos(piece, boardState.positions[piece.id], boardState)
        val field = boardState.positions[piece.id]
        console.debug("drawing piece ${piece.id} on field $field")
        val scaledPos = pos / PentaMath.R_ * scale
        val radius = (piece.radius / PentaMath.R_ * scale)
        val canMoveToFigure =
            if (selectedPlayerPieceField != null && field != null && selectedPlayerPieceField != field) {
                boardState.canMove(selectedPlayerPieceField, field)
            } else false
        when (piece) {
            is Piece.GrayBlocker -> {
                // TODO replace with 5-pointed shape
                circle {
                    // TODO: add path checking, only make reachable pieces clickable
                    if (isYourTurn && selectedPieceIsCurrentPlayer) {
                        cssClass = piece.id
                    }
                    cx = "${scaledPos.x}"
                    cy = "${scaledPos.y}"
                    r = "$radius"
                    fill = if (canMoveToFigure) {
                        piece.color.brighten()
                    } else {
                        piece.color
                    }.rgbHex
                }
            }
            is Piece.BlackBlocker -> {
                //TODO: replace with 7-pointed shape
                circle {
                    // TODO: add path checking, only make reachable pieces clickable
                    if (isYourTurn && selectedPieceIsCurrentPlayer) {
                        cssClass = piece.id
                    }
                    cx = "${scaledPos.x}"
                    cy = "${scaledPos.y}"

                    r = "$radius"
                    fill = if (canMoveToFigure) {
                        piece.color.brighten()
                    } else {
                        piece.color
                    }.rgbHex
                }
            }
            is Piece.Player -> {
                val playinguser = playingUsers[piece.player]
                val isCurrentPlayer = piece.player == boardState.currentPlayer
                console.debug("playerPiece: $piece")
                val selectable = isYourTurn
                        && isCurrentPlayer
                        && field != null
                        && boardState.selectedPlayerPiece == null
                val swappable = isYourTurn
                        && selectedPieceIsCurrentPlayer
                        && field != null
                console.info("playerPiece: ", piece, "selectable: ", selectable, "swappable", swappable)
                if (playinguser != null) {

                    drawPlayer(
//                    figureId = piece.figureId,
                        figureId = playinguser.figureId,
                        center = scaledPos,
                        radius = radius,
                        piece = piece,
                        selected = boardState.selectedPlayerPiece == piece,
                        highlight = selectable && !hasBlockerSelected,
                        clickable = selectable || swappable
                    )
                } else {

                }
            }
        }
    }

    // TODO add other info
}
