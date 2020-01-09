package components

import PentaBoard
import PentaMath
import containers.PentaVizDispatchProps
import containers.PentaVizStateProps
import io.data2viz.color.Colors
import io.data2viz.color.col
import io.data2viz.geom.Point
import io.data2viz.math.deg
import io.data2viz.scale.ScalesChromatic
import io.data2viz.viz.CircleNode
import io.data2viz.viz.PathNode
import io.data2viz.viz.TextHAlign
import io.data2viz.viz.TextNode
import io.data2viz.viz.TextVAlign
import io.data2viz.viz.Viz
import io.data2viz.viz.viz
import mu.KotlinLogging
import onHtmlRendered
import penta.PentaColor
import penta.client.PlayerCorner
import penta.logic.Piece
import penta.logic.field.AbstractField
import penta.logic.field.ConnectionField
import penta.logic.field.StartField
import penta.redux_rewrite.BoardState
import penta.util.length
import react.RBuilder
import react.RComponent
import react.RState
import penta.drawPlayer

interface PentaVizProps : PentaVizStateProps, PentaVizDispatchProps {
//    var boardState: BoardState
//    var addPlayerClick: () -> Unit
//    var startGameClick: () -> Unit
}

class PentaViz(props: PentaVizProps) : RComponent<PentaVizProps, RState>(props) {
    private val pieces: MutableMap<String, Pair<CircleNode, PathNode?>> = mutableMapOf()
    private val fieldElements = mutableMapOf<AbstractField, Triple<CircleNode, TextNode, TextNode?>>()
    private var playerCorners: List<PlayerCorner> = listOf()
    private lateinit var currentPlayerMarker: CircleNode
    private var scale: Double = 100.0
    private var mousePos: Point = Point(0.0, 0.0)

    override fun RBuilder.render() {
        vizCanvas {
            applyViz(this@PentaViz, props, attrs)

            //TODO: make canvas square
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private fun RBuilder.applyViz(component: PentaViz, props: PentaVizProps, attrs: VizProps) {
        fun f() {
            attrs.viz?.apply {
                playerCorners.forEach { corner ->
                    corner.face.remove()
                    corner.graySlot.remove()
                }
                // clear old pieces
                pieces.values.forEach { (circle, path) ->
                    circle.remove()
                    path?.remove()
                }
                pieces.clear()

                playerCorners = props.boardState.players.map {
                    logger.debug { ("init face $it") }
                    PlayerCorner(
                        it,
                        path {
                            drawPlayer(it.figureId, Point(0.0,0.0), PentaMath.s)
                        },
                        circle {
                            visible = false
                            fill = Colors.Web.lightgrey.brighten(0.5)
                            stroke = 0.col
                            strokeWidth = 1.0
                        }
                    )
                }
                if (::currentPlayerMarker.isInitialized) {
                    currentPlayerMarker.remove()
                }
                currentPlayerMarker = circle {
                    stroke = 0.col
                    strokeWidth = 3.0
                }

                // init pieces
                props.boardState.figures.forEach { piece ->
                    logger.debug { ("initialzing piece: $piece") }
                    val c = circle {
                        strokeWidth = 4.0
                        stroke = piece.color
                    }

                    val p =
                        if (piece is Piece.Player) {
                            path {
                                vAlign = TextVAlign.MIDDLE
                                hAlign = TextHAlign.MIDDLE

                                strokeWidth = 2.0
                                stroke = Colors.Web.black
                            }
                        } else null

                    pieces[piece.id] = Pair(c, p)

                    updatePiece(piece, props.boardState)
                }
                updateBoard(render = true)
            }
        }
        onHtmlRendered.add {
            logger.info { "initializing viz" }
            attrs.viz = viz {
                logger.info { ("height: $height") }
                logger.info { ("width: $width") }
                val scaleHCL = ScalesChromatic.Continuous.linearHCL {
                    domain = PentaColor.values().map { it.ordinal * 72.0 * 3 }
                    range = PentaColor.values().map { it.color }
                }
                val backgroundCircle = circle {
                    stroke = Colors.Web.black
                    fill = 0x28292b.col
                }
                val outerCircle = circle {
                    stroke = Colors.Web.lightgrey
                    strokeWidth = 4.0
                }
                PentaBoard.fields.forEach { field ->
                    logger.debug { ("adding: $field") }
                    val c = circle {
                        if (field is StartField) {
                            strokeWidth = 5.0
                            stroke = field.color
                            fill = Colors.Web.lightgrey
                        } else {
                            fill = field.color
                        }
                    }
                    val t1 = text {
                        fontSize -= 2
                        hAlign = TextHAlign.MIDDLE
                        vAlign = TextVAlign.BASELINE
                        textContent = field.id

                        visible = false
                    }

                    val t2 = if (field is ConnectionField) {
                        text {
                            fontSize -= 2
                            hAlign = TextHAlign.MIDDLE
                            vAlign = TextVAlign.HANGING
                            textContent = field.altId
                            visible = false
                        }
                    } else null
                    component.fieldElements[field] = Triple(c, t1, t2)
                }

                onResize { newWidth, newHeight ->
                    component.scale = kotlin.math.min(newWidth, newHeight)

                    findPiecesAtPos(component.mousePos).firstOrNull()
                        ?: PentaBoard.findFieldAtPos(component.mousePos)

                    backgroundCircle.apply {
                        x = 0.5 * component.scale
                        y = 0.5 * component.scale

                        radius = (1.0 * component.scale) / 2
                    }
                    outerCircle.apply {
                        x = 0.5 * component.scale
                        y = 0.5 * component.scale

                        radius = (PentaMath.r / PentaMath.R_ * component.scale) / 2
                    }

                    updateCorners()

                    // do not highlight blocker pieces or pieces that are out of the game
//            val highlightedPiece = highlightedPieceAt(mousePos)
//            val highlightedField = if (highlightedPiece == null) PentaBoard.findFieldAtPos(mousePos) else null

                    component.fieldElements.forEach { (field, triple) ->
                        val (circle, text1, text2) = triple
                        with(circle) {
                            x = ((field.pos.x / PentaMath.R_)) * component.scale
                            y = ((field.pos.y / PentaMath.R_)) * component.scale
                            radius = (field.radius / PentaMath.R_ * component.scale) - (strokeWidth ?: 0.0)
                        }
                        text1.apply {
                            x = ((field.pos.x / PentaMath.R_)) * component.scale
                            y = ((field.pos.y / PentaMath.R_)) * component.scale
                        }
                        text2?.apply {
                            x = ((field.pos.x / PentaMath.R_)) * component.scale
                            y = ((field.pos.y / PentaMath.R_)) * component.scale
                        }
                    }

                    props.boardState.figures.forEach {
                        updatePiece(it, props.boardState)
                    }
                }
            }
            f()
        }

        f()


    }

    fun updateCorners() {
        val boardState = props.boardState
        logger.trace { ("gameState.currentPlayer: ${boardState.currentPlayer}") }
        playerCorners.forEachIndexed { index, corner ->
            val angle = (-45 + (index) * 90).deg

            val radius = (PentaMath.R_ + (3 * PentaMath.s)) / PentaMath.R_ * scale / 2

            val facePos = Point(
                angle.cos * radius,
                angle.sin * radius
            ) + Point(0.5 * scale, 0.5 * scale)

            val pieceRadius = (PentaMath.s / PentaMath.R_ * scale) / 2
            logger.trace { ("face position: $facePos") }

            corner.graySlot.apply {
                visible = boardState.selectingGrayPiece && boardState.currentPlayer.id == corner.player.id
                if (visible) {
                    val pos = Point(
                        (angle + 10.deg).cos * radius,
                        (angle + 10.deg).sin * radius
                    ) + Point(0.5 * scale, 0.5 * scale)

                    x = pos.x
                    y = pos.y

                    stroke = Colors.Web.grey
                    strokeWidth = 2.0
//                    fill = Colors.Web.lightgrey.brighten(0.3)
                    fill = Colors.Web.white

                    this.radius = (PentaMath.s / 2.5) / PentaMath.R_ * scale
                }
            }
            logger.trace { ("player[$index]: ${corner.player}") }
            if (boardState.currentPlayer.id == corner.player.id) {
                currentPlayerMarker.apply {
                    x = facePos.x
                    y = facePos.y
                    this.radius = pieceRadius * 2
                }
            }

            corner.face.apply {
                stroke = 0.col
                fill = Colors.Web.black

                drawPlayer(figureId = corner.player.figureId, center = facePos, radius = pieceRadius)
            }

            // TODO: update place of nodes
        }
    }
    
    fun findPiecesAtPos(mousePos: Point) = props.boardState.figures.filter {
        (it.pos - mousePos).length < it.radius
    }
    
    fun highlightedPieceAt(mousePos: Point): Piece? = findPiecesAtPos(mousePos).firstOrNull()?.let {
        val boardState = props.boardState
        // do not highlight pieces that are off the board
        if (boardState.positions[it.id] == null) return@let null
        // allow highlighting blockers when a piece is selected
        if (it !is Piece.Player && boardState.selectedPlayerPiece == null) return@let null
        if (it is Piece.Player && boardState.currentPlayer.id != it.playerId) return@let null

        // remove highlighting pieces when placing a blocker
        if (
            (boardState.selectedGrayPiece != null || boardState.selectedBlackPiece != null || boardState.selectingGrayPiece)
            && it is Piece.Player
        ) return@let null

        it
    }

    fun Viz.updateBoard(render: Boolean = true) {
        val boardState = props.boardState
        // TODO: background: #28292b
//        turnDisplay.apply {
//            val turn = boardState.turn
//            value = "Turn: $turn" +
//                if (boardState.winner != null) ", winner: ${boardState.winner}" else ""
////                    + when {
////                        gameState.selectedPlayerPiece != null -> "move PlayerPiece (${gameState.selectedPlayerPiece!!.id})"
////                        gameState.selectedBlackPiece != null -> "set black (${gameState.selectedBlackPiece!!.id})"
////                        gameState.selectedGrayPiece != null -> "set grey (${gameState.selectedGrayPiece!!.id})"
////                        gameState.selectingGrayPiece -> "select gray piece"
////                        else -> "select Piece.Player"
////                    }
//        }
        updateCorners()
//        centerDisplay.second.textContent = turnDisplay.textContent
        if (render && renderer != null) {
            render()
        }
    }

    fun updatePiece(piece: Piece, boardState: BoardState) {
//        val boardState = gameState.boardStore.state
        val highlightedPiece = highlightedPieceAt(mousePos)

        val (circle, path) = pieces[piece.id] ?: throw IllegalArgumentException("piece; $piece is not on the board")

        // TODO: highlight player pieces on turn when not placing black or gray

        val pos = piece.pos
        val fillColor = when (piece) {
            is Piece.Player -> {
                if (
                    boardState.selectedPlayerPiece == null
                    && boardState.currentPlayer.id == piece.playerId
                    && penta.canClickPiece(piece, props.boardState)
                )
                    piece.color.brighten(1.0)
                else
                    piece.color
            }
            is Piece.BlackBlocker -> piece.color
            is Piece.GrayBlocker -> piece.color
            else -> throw IllegalStateException("unknown type ${piece::class}")
        }
        with(circle) {
            x = ((pos.x / PentaMath.R_)) * scale
            y = ((pos.y / PentaMath.R_)) * scale

            if (piece is Piece.Player && path != null) {
                visible = false
            }

            fill = fillColor
            stroke = fillColor.let {
                when (piece) {
                    boardState.selectedPlayerPiece -> {
                        strokeWidth = 3.0
                        it.brighten(2.0)
                    }
                    boardState.selectedBlackPiece -> {
                        strokeWidth = 3.0
                        it //.brighten(2.0)
                    }
//                    gameState.selectedGrayPiece -> if(gameState.selectedGrayPiece == null) it.brighten(1.0) else it
                    boardState.selectedGrayPiece -> {
                        strokeWidth = 3.0
                        it //.brighten(1.0)
                    }
                    highlightedPiece -> {
                        strokeWidth = 3.0
                        it.brighten(2.0)
                    }
                    else -> {
                        strokeWidth = 1.0
                        Colors.Web.black
                    }
                }
            }
            radius = (piece.radius / PentaMath.R_ * scale) - (strokeWidth ?: 0.0)
        }
//        text?.apply {
//            x = ((pos.x / PentaMath.R_)) * scale
//            y = ((pos.y / PentaMath.R_)) * scale
//            vAlign = TextVAlign.MIDDLE
//            hAlign = TextHAlign.MIDDLE
//            visible = false
//        }
        path?.apply {
            val playerPiece = piece as? Piece.Player ?: throw IllegalStateException("piece should be a playerpiece")
            val x = ((pos.x / PentaMath.R_)) * scale
            val y = ((pos.y / PentaMath.R_)) * scale
            val maxRadius = (playerPiece.radius / PentaMath.R_ * scale)
            drawPlayer(figureId = playerPiece.figureId, center = Point(x = x, y = y), radius = maxRadius)
            fill = fillColor
            stroke = circle.stroke
        }
    }
}


