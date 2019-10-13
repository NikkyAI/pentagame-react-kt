import com.lightningkite.reacktive.property.StandardObservableProperty
import io.data2viz.color.Colors
import io.data2viz.color.col
import io.data2viz.geom.Point
import io.data2viz.math.Angle
import io.data2viz.math.deg
import io.data2viz.scale.ScalesChromatic
import io.data2viz.viz.CircleNode
import io.data2viz.viz.KPointerClick
import io.data2viz.viz.KPointerMove
import io.data2viz.viz.PathNode
import io.data2viz.viz.TextHAlign
import io.data2viz.viz.TextNode
import io.data2viz.viz.TextVAlign
import io.data2viz.viz.Viz
import io.data2viz.viz.viz
import mu.KotlinLogging
import penta.ClientGameState
import penta.PentaColor
import penta.client.PlayerCorner
import penta.logic.Piece
import penta.logic.field.AbstractField
import penta.logic.field.ConnectionField
import penta.logic.field.CornerField
import kotlin.math.pow
import kotlin.math.sqrt

object PentaViz {
    private val logger = KotlinLogging.logger {}

    private val pieces: MutableMap<String, Pair<CircleNode, PathNode?>> = mutableMapOf()
    val elements = mutableMapOf<AbstractField, Triple<CircleNode, TextNode, TextNode?>>()
    private var playerCorners: List<PlayerCorner> = listOf()
    private lateinit var currentPlayerMarker: CircleNode
    private var scale: Double = 100.0
    private var mousePos: Point = Point(0.0, 0.0)
    val turnDisplay: StandardObservableProperty<String> = StandardObservableProperty("")
    val gameState = ClientGameState()
//    lateinit var centerDisplay: Pair<CircleNode, TextNode>

    fun highlightedPieceAt(mousePos: Point): Piece? = gameState.findPiecesAtPos(mousePos).firstOrNull()?.let {
        // do not highlight pieces that are off the board
        if (gameState.figurePositions[it.id] == null) return@let null
        // allow highlighting blockers when a piece is selected
        if (it !is Piece.Player && gameState.selectedPlayerPiece == null) return@let null
        if (it is Piece.Player && gameState.currentPlayer.id != it.playerId) return@let null

        // remove highlighting pieces when placing a blocker
        if (
            (gameState.selectedGrayPiece != null || gameState.selectedBlackPiece != null || gameState.selectingGrayPiece)
            && it is Piece.Player
        ) return@let null

        it
    }

    val viz = viz {
        logger.info { ("height: $height") }
        logger.info { ("width: $width") }
        val scaleHCL = ScalesChromatic.Continuous.linearHCL {
            domain = PentaColor.values().map { it.ordinal * 72.0 * 3 }
            range = PentaColor.values().map { it.color }
        }
//        turnDisplay = StandardObservableProperty("") {
//            vAlign = TextVAlign.HANGING
//            hAlign = TextHAlign.LEFT
//            fontSize += 4
//        }
        val backgroundCircle = circle {
            stroke = Colors.Web.black
            fill = 0x28292b.col
        }
        val outerCircle = circle {
            stroke = Colors.Web.lightgrey
//            fill = Colors.Web.lightgrey
            strokeWidth = 4.0
//            this.fill = Colors.Web.white
        }
        PentaBoard.fields.forEach { field ->
            logger.debug { ("adding: $field") }
            val c = circle {
                if (field is CornerField) {
                    strokeWidth = 5.0
                    stroke = field.color
                    fill = Colors.Web.lightgrey
                } else {
//                    strokeWidth = 0.0
//                    stroke = 0.col
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
            elements[field] = Triple(c, t1, t2)
        }

        onResize { newWidth, newHeight ->
            scale = kotlin.math.min(newWidth, newHeight)

            gameState.findPiecesAtPos(mousePos).firstOrNull()
                ?: PentaBoard.findFieldAtPos(mousePos)

            backgroundCircle.apply {
                x = 0.5 * scale
                y = 0.5 * scale

                radius = (1.0 * scale) / 2
            }
            outerCircle.apply {
                x = 0.5 * scale
                y = 0.5 * scale

                radius = (PentaMath.r / PentaMath.R_ * scale) / 2
            }

            updateCorners()

            // do not highlight blocker pieces or pieces that are out of the game
//            val highlightedPiece = highlightedPieceAt(mousePos)
//            val highlightedField = if (highlightedPiece == null) PentaBoard.findFieldAtPos(mousePos) else null

            elements.forEach { (field, triple) ->
                val (circle, text1, text2) = triple
                with(circle) {
                    x = ((field.pos.x / PentaMath.R_)) * scale
                    y = ((field.pos.y / PentaMath.R_)) * scale
                    radius = (field.radius / PentaMath.R_ * scale) - (strokeWidth ?: 0.0)
                }
                text1.apply {
                    x = ((field.pos.x / PentaMath.R_)) * scale
                    y = ((field.pos.y / PentaMath.R_)) * scale
                }
                text2?.apply {
                    x = ((field.pos.x / PentaMath.R_)) * scale
                    y = ((field.pos.y / PentaMath.R_)) * scale
                }
            }

            if (gameState.initialized) {
                gameState.figures.forEach {
                    updatePiece(it)
                }
            }
        }
    }

    fun updateCorners() {
        logger.trace { ("gameState.currentPlayer: ${gameState.currentPlayer}") }
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
                visible = gameState.selectingGrayPiece && gameState.currentPlayer.id == corner.player.id
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
            if (gameState.currentPlayer.id == corner.player.id) {
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

    fun resetBoard() {
        gameState.updatePiece = ::updatePiece

        viz.apply {
            // clear old pieces
            pieces.values.forEach { (circle, path) ->
                circle.remove()
                path?.remove()
            }
            pieces.clear()

            playerCorners = gameState.players.map {
                logger.debug { ("init face $it") }
                PlayerCorner(
                    it,
                    path {
                        //                        drawPlayer(it.figureId, Point(0.0,0.0), PentaMath.s)
                    },
                    circle {
                        visible = false
                        fill = Colors.Web.lightgrey.brighten(0.5)
                        stroke = 0.col
                        strokeWidth = 1.0
                    }
                )
            }
            currentPlayerMarker = circle {
                stroke = 0.col
                strokeWidth = 3.0
            }

            // init pieces
            gameState.figures.forEach { piece ->
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

                updatePiece(piece)
            }
            updateBoard()

            // trigger a resize event
//            val scale = kotlin.math.min(width, height)
//            resize(scale, scale)
        }
    }

    fun recolor() {
        val highlightedPiece = highlightedPieceAt(mousePos)
        val highlightedField = if (highlightedPiece == null) PentaBoard.findFieldAtPos(mousePos) else null
        elements.forEach { (field, triple) ->
            val (circle, text1, text2) = triple
            with(circle) {
                val c = if (field == highlightedField && gameState.canClickField(field))
                    field.color.brighten(2.0)
                else
                    field.color

                if (field is CornerField) {
                    stroke = c
                } else {
                    fill = c
                }
            }
        }
        if (gameState.initialized) {
            gameState.figures.forEach {
                updatePiece(it)
            }
        }
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
        }
        closePath()
    }

    fun updateBoard(render: Boolean = true) {
        // TODO: background: #28292b
        turnDisplay.apply {
            val turn = gameState.turn
            value = "Turn: $turn" +
                if (gameState.winner != null) ", winner: ${gameState.winner}" else ""
//                    + when {
//                        gameState.selectedPlayerPiece != null -> "move PlayerPiece (${gameState.selectedPlayerPiece!!.id})"
//                        gameState.selectedBlackPiece != null -> "set black (${gameState.selectedBlackPiece!!.id})"
//                        gameState.selectedGrayPiece != null -> "set grey (${gameState.selectedGrayPiece!!.id})"
//                        gameState.selectingGrayPiece -> "select gray piece"
//                        else -> "select Piece.Player"
//                    }
        }
        updateCorners()
//        centerDisplay.second.textContent = turnDisplay.textContent
        if (render) {
            viz.render()
        }
    }

    fun updatePiece(piece: Piece) {
        val highlightedPiece = highlightedPieceAt(mousePos)

        val (circle, path) = pieces[piece.id] ?: throw IllegalArgumentException("piece; $piece is not on the board")

        // TODO: highlight player pieces on turn when not placing black or gray

        val pos = piece.pos
        val fillColor = when (piece) {
            is Piece.Player -> {
                if (
                    gameState.selectedPlayerPiece == null
                    && gameState.currentPlayer.id == piece.playerId
                    && gameState.canClickPiece(piece)
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
                    gameState.selectedPlayerPiece -> {
                        strokeWidth = 3.0
                        it.brighten(2.0)
                    }
                    gameState.selectedBlackPiece -> {
                        strokeWidth = 3.0
                        it //.brighten(2.0)
                    }
//                    gameState.selectedGrayPiece -> if(gameState.selectedGrayPiece == null) it.brighten(1.0) else it
                    gameState.selectedGrayPiece -> {
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

    var hoveredField: AbstractField? = null
    var hoveredPiece: Piece? = null

    fun Viz.addEvents() {
        on(KPointerMove) { evt ->

            // convert pos back
            mousePos = (evt.pos / scale) * PentaMath.R_

            // show text on hovered fields
            PentaBoard.findFieldAtPos(mousePos).let { hoverField ->
                elements.forEach { (field, triple) ->
                    val (_, text1, text2) = triple

                    if (field == hoverField) {
                        text1.visible = true
                        text2?.visible = true
                    } else {
                        text1.visible = false
                        text2?.visible = false
                    }
                }
            }

            gameState.findPiecesAtPos(mousePos).firstOrNull()
                ?.let { piece ->
                    if (
                        (piece !is Piece.Player)
                        || (piece.playerId != gameState.currentPlayer.id)
                    ) {
                        hoveredPiece = null
//                        recolor()
//                        viz.render()
                        return@let null
                    }
                    if (hoveredPiece != piece) {
                        // TODO: canClick for highlighting
                        // can only highlight player piece
                        logger.trace { ("hover piece: $piece") }

                        hoveredPiece = piece
                        hoveredField = null

                        // TODO: refactor - instead of resize create `recolor()`
//                        viz.resize(viz.width, viz.height)
                        recolor()
                        viz.render()
                    }
                    piece
                }
                ?: PentaBoard.findFieldAtPos(mousePos)?.let {
                    if (
                        gameState.selectedPlayerPiece == null
                        && gameState.selectedGrayPiece == null
                        && gameState.selectedBlackPiece == null
                    ) {
                        hoveredField = null
                        hoveredPiece = null
                        recolor()
                        viz.render()
                        return@let null
                    }
                    if (hoveredField != it) {
//                        logger.trace { "hover field: $it" }

                        hoveredField = it
                        hoveredPiece = null

                        // TODO: refactor - instead of resize create `recolor()`
//                        viz.resize(viz.width, viz.height)
                        recolor()
                        viz.render()
                    }
                    it
                }
                ?: run {
                    //  logger.trace { "Mouse Move:: ${mousePos}" }
                    hoveredField = null
                    hoveredPiece = null
                    recolor()
                    viz.render()
                }
        }
        on(KPointerClick) { evt ->
            logger.trace { ("MouseClick:: $evt") }
//            logger.trace { ("shiftKey:: ${evt.shiftKey}") }
//            logger.trace { ("ctrlKey:: ${evt.ctrlKey}") }
//            logger.trace { ("metaKey:: ${evt.metaKey}") }
//            logger.trace { ("ctrlKey:: ${evt.ctrlKey}") }
            mousePos = (evt.pos / scale) * PentaMath.R_

            val piece = gameState.findPiecesAtPos(mousePos).firstOrNull()
            if (piece != null) {
                logger.debug { ("clickPiece($piece)") }
                gameState.clickPiece(piece)
            } else {
                val field = PentaBoard.findFieldAtPos(mousePos)
                if (field != null) {
                    gameState.clickField(field)
                }
            }
        }
    }
}