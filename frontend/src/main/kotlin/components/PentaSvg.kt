package components

import PentaBoard
import PentaMath
import com.github.nwillc.ksvg.elements.SVG
import containers.PentaSvgDispatchProps
import containers.PentaSvgStateProps
import debug
import io.data2viz.color.Colors
import io.data2viz.color.col
import io.data2viz.math.deg
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.w3c.dom.Element
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.svg.SVGCircleElement
import org.w3c.dom.svg.SVGElement
import penta.PentaColors
import penta.ConnectionState
import penta.PentaMove
import penta.PentagameClick
import penta.drawFigure
import penta.drawPlayer
import penta.calculatePiecePos
import penta.cornerPoint
import penta.logic.Piece
import penta.logic.field.ConnectionField
import penta.logic.field.GoalField
import penta.logic.field.StartField
import react.RBuilder
import react.RComponent
import react.RState
import react.createRef
import react.dom.button
import react.dom.svg
import kotlin.browser.document
import kotlin.dom.clear

interface PentaSvgProps : PentaSvgStateProps, PentaSvgDispatchProps

class PentaSvg(props: PentaSvgProps) : RComponent<PentaSvgProps, RState>(props) {
    private val svgRef = createRef<SVGElement>()
    private val buttonRef = createRef<HTMLButtonElement>()

    fun dispatchMove(move: PentaMove) {
        when(val connection = props.connection) {
            is ConnectionState.Observing -> {
                when(move) {
                    // unsupported by old backend
                    is PentaMove.SelectGrey -> props.dispatchMoveLocal(move)
                    is PentaMove.SelectPlayerPiece -> props.dispatchMoveLocal(move)
                    else -> {
                        GlobalScope.launch {
                            connection.sendMove(move)
                        }
                    }
                }
            }
            else -> {
                props.dispatchMoveLocal(move)
            }
        }
    }
    override fun RBuilder.render() {
        svg {
            ref = svgRef
        }
        button {
            +"svg file"
            ref = buttonRef
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun componentDidMount() {
        console.log("penta svg mounted")

        buttonRef.current?.onclick = {
            val scale = 1000

            val svg = SVG.svg {
                viewBox = "0 0 $scale $scale"

                draw(scale, props)
            }

            val svgFile = buildString {
                svg.render(this, SVG.RenderMode.FILE)
            }

            console.log(svgFile)
        }

        redraw(props)
    }

    override fun shouldComponentUpdate(nextProps: PentaSvgProps, nextState: RState): Boolean {
        // TODO update svg content here
        console.log("penta svg updating")
//        console.log("props: ${props.boardState}")
//        console.log("nextProps: ${nextProps.boardState}")

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
                    it.render(this, SVG.RenderMode.INLINE)
                }
            }
            svg.innerHTML = svgInline
            svg.setAttribute("width", "100%")
            svg.setAttribute("height", "100%")
            svg.setAttribute("viewBox", newSVG.viewBox!!)
            svg.setAttribute("preserveAspectRatio", "xMidYMid meet")
        }

        val clickFields = { event: Event ->
            event as MouseEvent
            console.log("event $event ")
            console.log("target ${event.target} ")
            when (val target = event.target) {
                is SVGCircleElement -> {
                    if (target.id.isNotBlank()) {
                        if (PentaBoard.fields.any { it.id == target.id }) {
                            // TODO: clickfield
                            console.log("clicked field ${target.id}")
                            PentagameClick.clickField(
                                PentaBoard.fields.first { it.id == target.id },
                                ::dispatchMove,
                                svgProps.boardState
                            )
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
                    if (target.id.isNotBlank()) {
                        console.log("clicked piece ${target.id}")
                        console.log(target::class.js)
                        PentagameClick.clickPiece(
                            props.boardState.figures.first { it.id == target.id },
                            ::dispatchMove,
                            svgProps.boardState
                        )
                    }
                }
            }
        }

        PentaBoard.fields.forEach {
            document.getElementById(it.id)?.addEventListener("click", clickFields, true)
        }
        props.boardState.figures.forEach {
            document.getElementById(it.id)?.let { el ->
                el.addEventListener("click", clickPieces, true)
            }
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

    val isYourTurn = if(svgProps.connection is ConnectionState.Observing) {
        svgProps.boardState.currentPlayer.id == svgProps.connection.userId
    } else true
    val selectedPieceIsCurrentPlayer = svgProps.boardState.selectedPlayerPiece?.playerId == svgProps.boardState.currentPlayer.id

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

    PentaBoard.fields.forEach { field ->
        val scaledPos = field.pos / PentaMath.R_ * scale
        val radius = (field.radius / PentaMath.R_ * scale)
        when (field) {
            is StartField -> {
                circle {
                    // TODO: add path checking, only make reachable pieces clickable
                    if(isYourTurn && selectedPieceIsCurrentPlayer) {
                        id = field.id
                    }
                    cx = "${scaledPos.x}"
                    cy = "${scaledPos.y}"
                    r = "${radius - lineWidth}"
                    stroke = field.color.rgbHex
                    strokeWidth = lineWidth.toString()
                    fill = PentaColors.FOREGROUND.rgbHex
                }
            }
            is GoalField -> {
                circle {
                    // TODO: add path checking, only make reachable pieces clickable
                    if(isYourTurn && selectedPieceIsCurrentPlayer) {
                        id = field.id
                    }
                    cx = "${scaledPos.x}"
                    cy = "${scaledPos.y}"
                    r = "${radius - (lineWidth / 2)}"
                    fill = field.color.rgbHex
//                    strokeWidth = "${scaledLineWidth / 10}"
//                    stroke = "#28292b"
                }
            }
            is ConnectionField -> {
                circle {
                    // TODO: add path checking, only make reachable pieces clickable
                    if(isYourTurn && selectedPieceIsCurrentPlayer) {
                        id = field.id
                    }
                    cx = "${scaledPos.x}"
                    cy = "${scaledPos.y}"
                    r = "${radius - (lineWidth / 10)}"
                    fill = field.color.rgbHex
                    strokeWidth = "${lineWidth / 2}"
                    stroke = PentaColors.BACKGROUND.rgbHex
                    asDynamic().onclick = { it: Any -> console.log("clicked $it ") }
                }
            }
        }
    }


    // add corner fields
    svgProps.boardState.players.forEachIndexed { i, player ->
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
            figureId = player.figureId,
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
        when (piece) {
            is Piece.GrayBlocker -> {
                circle {
                    // TODO: add path checking, only make reachable pieces clickable
                    if(isYourTurn && selectedPieceIsCurrentPlayer) {
                        id = piece.id
                    }
                    cx = "${scaledPos.x}"
                    cy = "${scaledPos.y}"
                    r = "$radius"
                    fill = piece.color.rgbHex
                }
            }
            is Piece.BlackBlocker -> {
                circle {
                    // TODO: add path checking, only make reachable pieces clickable
                    if(isYourTurn && selectedPieceIsCurrentPlayer) {
                        id = piece.id
                    }
                    cx = "${scaledPos.x}"
                    cy = "${scaledPos.y}"

                    r = "$radius"
                    fill = piece.color.rgbHex
                }
            }
            is Piece.Player -> {
                val isCurrentPlayer = piece.playerId == svgProps.boardState.currentPlayer.id
                console.debug("playerPiece: $piece")
                val selectable = isYourTurn && isCurrentPlayer && svgProps.boardState.selectedPlayerPiece == null
                val swappable = isYourTurn && selectedPieceIsCurrentPlayer
                console.info("playerPiece: ", piece, "selectable: ", selectable, "swappable", swappable)
                drawPlayer(
                    figureId = piece.figureId,
                    center = scaledPos,
                    radius = radius,
                    piece = piece,
                    selected = svgProps.boardState.selectedPlayerPiece == piece,
                    highlight = selectable,
                    clickable = selectable || swappable
                )
            }
        }
    }

    // TODO add other info
}



