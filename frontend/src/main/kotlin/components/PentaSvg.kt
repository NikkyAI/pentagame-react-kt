package components

import PentaBoard
import PentaMath
import com.ccfraser.muirwik.components.MColor
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import com.github.nwillc.ksvg.RenderMode
import com.github.nwillc.ksvg.elements.SVG
import containers.PentaSvgDispatchProps
import containers.PentaSvgStateProps
import debug
import io.data2viz.color.Colors
import io.data2viz.color.col
import io.data2viz.math.deg
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.Element
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.svg.SVGCircleElement
import org.w3c.dom.svg.SVGElement
import penta.BoardState
import penta.ConnectionState
import penta.PentaColors
import penta.PentaMove
import penta.PentagameClick
import penta.PlayerState
import penta.UserInfo
import penta.calculatePiecePos
import penta.cornerPoint
import penta.drawFigure
import penta.drawPlayer
import penta.logic.Field
import penta.logic.Field.ConnectionField
import penta.logic.Field.Goal
import penta.logic.Field.Start
import penta.logic.Piece
import react.RBuilder
import react.RComponent
import react.RState
import react.createRef
import styled.styledSvg
import util.forEach
import kotlin.dom.clear

interface PentaSvgProps : PentaSvgStateProps, PentaSvgDispatchProps

class PentaSvg(props: PentaSvgProps) : RComponent<PentaSvgProps, RState>(props) {
    private val svgRef = createRef<SVGElement>()

    fun dispatchMove(move: PentaMove) {
        when (val connection = props.connection) {
            is ConnectionState.ConnectedToGame -> {
//                when (move) {
//                    // unsupported by old backend
////                    is PentaMove.SelectGrey -> props.dispatchMoveLocal(move)
////                    is PentaMove.SelectPlayerPiece -> props.dispatchMoveLocal(move)
//                    else -> {
                GlobalScope.launch {
                    connection.sendMove(move)
                }
//                    }
//                }
            }
            else -> {
                props.dispatchMoveLocal(move)
            }
        }
    }

    override fun RBuilder.render() {
        styledSvg {
            ref = svgRef
            attrs {
                attributes["preserveAspectRatio"] = "xMidYMid meet"
//                attributes["width"] = "100vw"
//                attributes["height"] = "100vh"
            }
        }
        //TODO: move to DebugGame view
        mButton(
            caption = "svg file",
            variant = MButtonVariant.contained,
            color = MColor.primary,
            onClick = {
                val scale = 1000

                val svg = SVG.svg {
                    viewBox = "0 0 $scale $scale"

                    draw(scale, props)
                }

                val svgFile = buildString {
                    svg.render(this, RenderMode.FILE)
                }

                console.log(svgFile)
            }
        )
    }

    override fun componentDidMount() {
        console.log("penta svg mounted")

        redraw(props)
    }

    override fun shouldComponentUpdate(nextProps: PentaSvgProps, nextState: RState): Boolean {
        // TODO update svg content here
        console.log("penta svg updating")
//        console.log("props: ${props.boardState}")
//        console.log("nextProps: ${nextProps.boardState}")

        // access isPlayBack
        when (val conn = nextProps.connection) {
            is ConnectionState.ConnectedToGame -> {
                if (conn.isPlayback) return false
            }
        }
        redraw(nextProps)

        return false
    }

    private fun redraw(svgProps: PentaSvgProps) {
        console.debug("drawing...")

        // does SVG stuff
        svgRef.current?.let { svg ->
            svg.clear()

            val scale = 1000

            val newSVG = SVG.svg {
                viewBox = "0 0 $scale $scale"

                draw(scale, svgProps)
            }
//            val fullSvg = buildString {
//                newSVG.render(this, SVG.RenderMode.INLINE)
//            }
//            console.log("svg: ${fullSvg}")
            val svgInline = buildString {
                newSVG.children.forEach {
                    it.render(this, RenderMode.INLINE)
                }
            }
            svg.innerHTML = svgInline
            svg.setAttribute("viewBox", newSVG.viewBox!!)
        }

        val clickFields = { event: Event ->
            event as MouseEvent
            console.log("event $event ")
            console.log("target ${event.target} ")
            when (val target = event.target) {
                is SVGCircleElement -> {
                    target.classList.forEach { id ->
                        if (PentaBoard.fields.any { it.id == id }) {
                            // TODO: clickfield
                            console.log("clicked field $id")
                            PentagameClick.clickField(
                                PentaBoard.fields.firstOrNull { it.id == id }
                                    ?: throw IllegalStateException("target '${id}' cannot be found"),
                                ::dispatchMove,
                                svgProps.boardState
                            )
                            return@forEach
                        }
                    }
                }
            }
        }
        val clickPieces = { event: Event ->
            event as MouseEvent
            console.log("event $event ")
            console.log("target ${event.target} ")
            when (val target = event.target) {
                is Element -> {
                    target.classList.forEach { id ->
                        console.log("clicked piece ${id}")
                        PentagameClick.clickPiece(
                            props.boardState.figures.firstOrNull { it.id == id }
                                ?: throw IllegalStateException("target '$id' cannot be found"),
                            ::dispatchMove,
                            svgProps.boardState
                        )
                        return@forEach
                    }
                }
            }
            Unit
        }

        PentaBoard.fields.forEach {
            svgRef.current?.getElementsByClassName(it.id)?.item(0)
                ?.addEventListener("click", clickFields, true)
        }
        props.boardState.figures.forEach {
            svgRef.current?.getElementsByClassName(it.id)?.item(0)
                ?.addEventListener("click", clickPieces, true)
        }
    }
}

private fun SVG.draw(scale: Int, svgProps: PentaSvgProps) {
//        val lineWidth = (PentaMath.s / 5) / PentaMath.R_ * scale
    val lineWidth = 0.1 / PentaMath.R_ * scale

    console.debug("lineWidth: $lineWidth")
//        console.log("thinLineWidth: $thinLineWidth")

//        style {
//            body = """
//             svg .black-stroke { stroke: black; stroke-width: 2; }
//            """.trimIndent()
//        }
    val boardState = svgProps.boardState

    val isYourTurn = if (svgProps.connection is ConnectionState.ConnectedToGame) {
        svgProps.boardState.currentPlayer.id == svgProps.connection.userId
    } else true // in local game it is always your turn
    val selectedPieceIsCurrentPlayer = boardState.selectedPlayerPiece?.player == svgProps.boardState.currentPlayer
    val selectedPlayerPieceField = if (selectedPieceIsCurrentPlayer) {
        svgProps.boardState.positions[boardState.selectedPlayerPiece?.id] ?: run {
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

    val emptyFields = PentaBoard.fields - svgProps.boardState.positions.mapNotNull { (_, field) -> field }

    PentaBoard.fields.forEach { field ->
        val canMoveToField = if (selectedPlayerPieceField != null) {
            svgProps.boardState.canMove(selectedPlayerPieceField, field)
        } else false

        val canPlaceBlocker = hasBlockerSelected && field in emptyFields

        val scaledPos = field.pos / PentaMath.R_ * scale
        val radius = (field.radius / PentaMath.R_ * scale)
        when (field) {
            is Start -> {
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
            is Goal -> {
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
            is ConnectionField -> {
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
    svgProps.boardState.gameType.players.forEachIndexed { i, player ->
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
            if (player.id == svgProps.boardState.currentPlayer.id) {
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

        val playinguser = svgProps.playingUsers[player]
        if (playinguser != null) {
            // draw player face
            drawFigure(
                figureId = playinguser.figureId,
                center = centerPos,
                radius = pieceRadius,
                color = Colors.Web.black,
                selected = svgProps.boardState.currentPlayer.id == player.id
            )
        } else {
            console.warn("no player $player in ${svgProps.playingUsers}")
            //TODO: render placeholder figures
        }

        // show gray slot when selecting gray
        if (
            svgProps.boardState.currentPlayer.id == player.id &&
            svgProps.boardState.selectingGrayPiece
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

    svgProps.boardState.figures.forEach { piece ->
        val pos = calculatePiecePos(piece, svgProps.boardState.positions[piece.id], svgProps.boardState)
        val field = svgProps.boardState.positions[piece.id]
        console.debug("drawing piece ${piece.id} on field $field")
        val scaledPos = pos / PentaMath.R_ * scale
        val radius = (piece.radius / PentaMath.R_ * scale)
        val canMoveToFigure =
            if (selectedPlayerPieceField != null && field != null && selectedPlayerPieceField != field) {
                svgProps.boardState.canMove(selectedPlayerPieceField, field)
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
                val playinguser = svgProps.playingUsers[piece.player]
                val isCurrentPlayer = piece.player == svgProps.boardState.currentPlayer
                console.debug("playerPiece: $piece")
                val selectable = isYourTurn
                        && isCurrentPlayer
                        && field != null
                        && svgProps.boardState.selectedPlayerPiece == null
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
                        selected = svgProps.boardState.selectedPlayerPiece == piece,
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

fun SVG.drawPentagame(scale: Int, svgProps: DrawProps) {
    val lineWidth = 0.1 / PentaMath.R_ * scale

    console.debug("lineWidth: $lineWidth")

    val boardState = svgProps.boardState

    val isYourTurn = svgProps.isYourTurn ?: true
    val selectedPieceIsCurrentPlayer = boardState.selectedPlayerPiece?.player == svgProps.boardState.currentPlayer
    val selectedPlayerPieceField = if (selectedPieceIsCurrentPlayer) {
        svgProps.boardState.positions[boardState.selectedPlayerPiece?.id] ?: run {
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

    val emptyFields = PentaBoard.fields - svgProps.boardState.positions.mapNotNull { (_, field) -> field }

    PentaBoard.fields.forEach { field ->
        val canMoveToField = if (selectedPlayerPieceField != null) {
            svgProps.boardState.canMove(selectedPlayerPieceField, field)
        } else false

        val canPlaceBlocker = hasBlockerSelected && field in emptyFields

        val scaledPos = field.pos / PentaMath.R_ * scale
        val radius = (field.radius / PentaMath.R_ * scale)
        when (field) {
            is Start -> {
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
            is Goal -> {
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
            is ConnectionField -> {
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
    svgProps.boardState.gameType.players.forEachIndexed { i, player ->
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
            if (player.id == svgProps.boardState.currentPlayer.id) {
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

        // draw player face
        drawFigure(
            figureId = svgProps.playingUsers[player]!!.figureId,
            center = centerPos,
            radius = pieceRadius,
            color = Colors.Web.black,
            selected = svgProps.boardState.currentPlayer.id == player.id
        )

        // show gray slot when selecting gray
        if (
            svgProps.boardState.currentPlayer.id == player.id &&
            svgProps.boardState.selectingGrayPiece
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

    svgProps.boardState.figures.forEach { piece ->
        val pos = calculatePiecePos(piece, svgProps.boardState.positions[piece.id], svgProps.boardState)
        val field = svgProps.boardState.positions[piece.id]
        console.debug("drawing piece ${piece.id} on field $field")
        val scaledPos = pos / PentaMath.R_ * scale
        val radius = (piece.radius / PentaMath.R_ * scale)
        val canMoveToFigure =
            if (selectedPlayerPieceField != null && field != null && selectedPlayerPieceField != field) {
                svgProps.boardState.canMove(selectedPlayerPieceField, field)
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
                val isCurrentPlayer = piece.player == svgProps.boardState.currentPlayer
                console.debug("playerPiece: $piece")
                val selectable = isYourTurn
                        && isCurrentPlayer
                        && field != null
                        && svgProps.boardState.selectedPlayerPiece == null
                val swappable = isYourTurn
                        && selectedPieceIsCurrentPlayer
                        && field != null
                console.info("playerPiece: ", piece, "selectable: ", selectable, "swappable", swappable)
                val playinguser = svgProps.playingUsers[piece.player]
                if (playinguser != null) {
                    drawPlayer(
                        figureId = playinguser.figureId,
                        center = scaledPos,
                        radius = radius,
                        piece = piece,
                        selected = svgProps.boardState.selectedPlayerPiece == piece,
                        highlight = selectable && !hasBlockerSelected,
                        clickable = selectable || swappable
                    )
                }
            }
        }
    }

    // TODO add other info
}



