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
import penta.ClientGameState
import penta.PentaColor
import penta.logic.field.AbstractField
import penta.logic.field.ConnectionField
import penta.logic.Piece
import penta.logic.field.CornerField
import kotlin.math.pow
import kotlin.math.sqrt

object PentaViz {
    private val pieces: MutableMap<String, Pair<CircleNode, PathNode?>> = mutableMapOf()
    val elements = mutableMapOf<AbstractField, Triple<CircleNode, TextNode, TextNode?>>()
    private var scale: Double = 100.0
    private var mousePos: Point = Point(0.0, 0.0)
    lateinit var turnDisplay: TextNode
//    lateinit var centerDisplay: Pair<CircleNode, TextNode>

    fun highlightedPieceAt(mousePos: Point): Piece? = gameState.findPiecesAtPos(mousePos).firstOrNull()?.let {
        // do not highlight pieces that are off the board
        if (gameState.figurePositions[it.id] == null) return@let null
        // allow highlighting blockers when a piece is selected
        if (it !is Piece.Player && gameState.selectedPlayerPiece == null) return@let null
        if (it is Piece.Player && gameState.currentPlayer != it.playerId) return@let null

        // remove highlighting pieces when placing a blocker
        if (
            (gameState.selectedGrayPiece != null || gameState.selectedBlackPiece != null || gameState.selectingGrayPiece)
            && it is Piece.Player
        ) return@let null

        it
    }

    val viz = viz {
        println("height: $height")
        println("width: $width")
        val scaleHCL = ScalesChromatic.Continuous.linearHCL {
            domain = PentaColor.values().map { it.ordinal * 72.0 * 3 }
            range = PentaColor.values().map { it.color }
        }
        turnDisplay = text {
            vAlign = TextVAlign.HANGING
            hAlign = TextHAlign.LEFT
            fontSize += 4
        }
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
            println("adding: $field")
            val c = circle {
                if(field is CornerField) {
                    strokeWidth = 5.0
                    stroke = field.color
                    fill = Colors.Web.lightgrey
                } else {
                    strokeWidth = 1.0
                    stroke = 0.col
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
            //            println("resize")
//            println("height: $height > $newHeight")
//            println("width: $width > $newWidth")
            scale = kotlin.math.min(newWidth, newHeight)
//            println("scale: $scale")

            gameState.findPiecesAtPos(mousePos).firstOrNull()
                ?.also {
                    //                    println("hovered: $it")
                }
                ?: PentaBoard.findFieldAtPos(mousePos)?.also {
                    //                    println("hovered: $it")
                }
                ?: run {
                    //                    println("Mouse Move:: $mousePos")
                }

            val halfCircleWidth = (0.25 / PentaMath.R_) * scale / 2
//            outerCircle.forEachIndexed { index, lineNode ->
//                val angle = index.deg / 3
//
//                with(lineNode) {
//                    x1 = (PentaMath.r / PentaMath.R_ * scale / 2 - halfCircleWidth) * angle.cos + scale / 2
//                    x2 = (PentaMath.r / PentaMath.R_ * scale / 2 + halfCircleWidth) * angle.cos + scale / 2
//                    y1 = (PentaMath.r / PentaMath.R_ * scale / 2 - halfCircleWidth) * angle.sin + scale / 2
//                    y2 = (PentaMath.r / PentaMath.R_ * scale / 2 + halfCircleWidth) * angle.sin + scale / 2
//                }
//            }
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
//            PentaViz.centerDisplay.first.apply {
//                x = 0.5 * scale
//                y = 0.5 * scale
//                radius = (1.0 / PentaMath.R_ * scale)
//            }
//            PentaViz.centerDisplay.second.apply {
//                x = 0.5 * scale
//                y = 0.5 * scale
//            }
//        with(outerCircle) {
//            x = size / 2
//            y = size / 2
//            radius = (PentaMath.r / PentaMath.R_ * size / 2.0)
//        }

            // do not highlight blocker pieces or pieces that are out of the game
//            val highlightedPiece = highlightedPieceAt(mousePos)
//            val highlightedField = if (highlightedPiece == null) PentaBoard.findFieldAtPos(mousePos) else null

            elements.forEach { (field, triple) ->
                val (circle, text1, text2) = triple
                with(circle) {
                    x = ((field.pos.x / PentaMath.R_)) * scale
                    y = ((field.pos.y / PentaMath.R_)) * scale
                    radius = (field.radius / PentaMath.R_ * scale) //  - (strokeWidth ?: 0.0)
//                    fill = if (field != highlightedField)
//                        field.pentaColor
//                    else
//                        field.pentaColor.brighten(2.0)
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

            if(gameState.initialized) {
                gameState.figures.forEach {
                    updatePiece(it)
                }
            }

//            pieces.keys.forEach {
//                updatePiece(it)
//            }
        }
    }

    var gameState = ClientGameState(listOf(""))
//        set(gameState) {
//            gameState.updatePiece = ::updatePiece
//
//            viz.apply {
//                // clear old pieces
//                pieces.values.forEach { (circle, path) ->
//                    circle.remove()
//                    path?.remove()
//                }
//                pieces.clear()
//
//                // init pieces
//                gameState.figures.forEach { piece ->
//                    println("initialzing piece: $piece")
//                    val c = circle {
//                        strokeWidth = 4.0
//                        stroke = piece.color
//                    }
//
//                    val p =
//                        if (piece is Piece.Player) {
//                            path {
//                                vAlign = TextVAlign.MIDDLE
//                                hAlign = TextHAlign.MIDDLE
//
//                                strokeWidth = 2.0
//                                stroke = Colors.Web.black
//                            }
//                        } else null
//
//                    pieces[piece.id] = Pair(c, p)
//
//                    updatePiece(piece)
//                }
//            }
//            field = gameState
//            updateBoard(false)
//        }

    fun resetBoard() {
        gameState.updatePiece = ::updatePiece

        viz.apply {
            // clear old pieces
            pieces.values.forEach { (circle, path) ->
                circle.remove()
                path?.remove()
            }
            pieces.clear()

            // init pieces
            gameState.figures.forEach { piece ->
                println("initialzing piece: $piece")
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

                if(field is CornerField) {
                    stroke = c
                } else {
                    fill = c
                }
            }
        }
        if(gameState.initialized) {
            gameState.figures.forEach {
                updatePiece(it)
            }
        }

    }

    private fun PathNode.drawPlayer(playerId: String, center: Point, maxRadius: Double) {
        clearPath()

        fun point(angle: Angle, radius: Double, center: Point = Point(0.0,0.0)): Point {
            return Point(angle.cos * radius, angle.sin * radius) + center
        }

        fun angles(n: Int, start: Angle = 0.deg): List<Angle> {
            val step = 360.deg / n

            return (0..n).map { i ->
                (start + (step * i))
            }
        }

        when(playerId) {
            "square" -> {
                angles(4, 0.deg).map {  angle ->
                    val point = point(angle, maxRadius, center)
                    lineTo(point.x, point.y)
                }
            }
            "triangle" -> {
                angles(3, -90.deg).map {  angle ->
                    val point = point(angle, maxRadius, center)
                    lineTo(point.x, point.y)
                }
            }
            "cross" -> {

                val width = 15

                val p1 = point((45-width).deg, maxRadius, center)
                val p2 = point((45+width).deg, maxRadius, center)

                val c = sqrt((p2.x-p1.x).pow(2) + (p2.y-p1.y).pow(2))

                val a = c / sqrt(2.0)

                val points = listOf(
                    point((45-width).deg, maxRadius, center),
                    point((45+width).deg, maxRadius, center),
                    point((90).deg, a, center),
                    point((135-width).deg, maxRadius, center),
                    point((135+width).deg, maxRadius, center),
                    point((180).deg, a, center),
                    point((45 + 180 - width).deg, maxRadius, center),
                    point((45 + 180 + width).deg, maxRadius, center),
                    point((270).deg, a, center),
                    point((135 + 180 - width).deg, maxRadius, center),
                    point((135 + 180 + width).deg, maxRadius, center),
                    point((360).deg, a, center)
                )
                points.forEach {
                    lineTo(it.x, it.y)
                }


//                lineTo(center.x, center.y)
//                angles(2, 45.deg).map {  angle ->
//                    val point = point(angle, maxRadius, center)
//                    lineTo(point.x, point.y)
//                }
//                lineTo(center.x, center.y)
//                angles(2, 135.deg).map {  angle ->
//                    val point = point(angle, maxRadius, center)
//                    lineTo(point.x, point.y)
//                }
            }
            "circle" -> {
                arc(center.x, center.y, maxRadius, 0.0, 180.0, false)
            }
        }

        closePath()
    }

    fun updateBoard(render: Boolean = true) {
        // TODO: background: #28292b
        turnDisplay.apply {
            val player = gameState.currentPlayer
            val turn = gameState.turn
            textContent = "Player: $player, Turn: $turn, " +
                if (gameState.winner != null) ", winner: ${gameState.winner}" else "" +
                    when {
                        gameState.selectedPlayerPiece != null -> "move PlayerPiece (${gameState.selectedPlayerPiece!!.id})"
                        gameState.selectedBlackPiece != null -> "set black (${gameState.selectedBlackPiece!!.id})"
                        gameState.selectedGrayPiece != null -> "set grey (${gameState.selectedGrayPiece!!.id})"
                        gameState.selectingGrayPiece -> "select gray piece"
                        else -> "select Piece.Player"
                    }
        }
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
                    && gameState.currentPlayer == piece.playerId
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

            if(piece is Piece.Player && path != null) {
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
                        it.brighten(2.0)
                    }
//                    gameState.selectedGrayPiece -> if(gameState.selectedGrayPiece == null) it.brighten(1.0) else it
                    gameState.selectedGrayPiece -> {
                        strokeWidth = 3.0
                        it.brighten(1.0)
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
            drawPlayer(playerId = playerPiece.playerId, center = Point(x = x, y = y), maxRadius = maxRadius)
            fill = fillColor
            stroke = circle.stroke
        }
    }

    var hoveredField: AbstractField? = null
    var hoveredPiece: Piece? = null

    fun Viz.addEvents() {
        on(KPointerMove) { evt ->
            // println("Mouse Move:: ${evt.pos}")

            // convert pos back
//            mousePos = ((evt.pos / scale)- Point(0.5, 0.5)) * PentaMath.R_ * 2
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
                ?.let {
                    if (
                        (it !is Piece.Player)
                        || (it.playerId != gameState.currentPlayer)
                    ) {
                        hoveredPiece = null
//                        recolor()
//                        viz.render()
                        return@let null
                    }
                    if (hoveredPiece != it) {
                        // TODO: canClick for highlighting
                        // can only highlight player piece
                        println("hover piece: $it")

                        hoveredPiece = it
                        hoveredField = null

                        // TODO: refactor - instead of resize create `recolor()`
//                        viz.resize(viz.width, viz.height)
                        recolor()
                        viz.render()
                    }
                    it
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
                        println("hover field: $it")

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
                    //                    println("Mouse Move:: ${mousePos}")
//                    if (hoveredField != null || hoveredPiece != null) {
                    hoveredField = null
                    hoveredPiece = null
                    recolor()
                    viz.render()
//                    }
                }
        }
        on(KPointerClick) { evt ->
            //            println("MouseClick:: $evt")
//            println("shiftKey:: ${evt.shiftKey}")
//            println("ctrlKey:: ${evt.ctrlKey}")
//            println("metaKey:: ${evt.metaKey}")
//            println("ctrlKey:: ${evt.ctrlKey}")
            mousePos = (evt.pos / scale) * PentaMath.R_

            val piece = gameState.findPiecesAtPos(mousePos).firstOrNull()
            if (piece != null) {
                println("clickPiece($piece)")
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