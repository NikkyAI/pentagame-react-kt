package penta.client

import PentaBoard
import PentaMath
import io.data2viz.color.Colors
import io.data2viz.color.col
import io.data2viz.geom.Point
import io.data2viz.math.Angle
import io.data2viz.math.deg
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
import penta.ConnectionState
import penta.canClickPiece
import penta.drawFigure
import penta.logic.Piece
import penta.logic.field.AbstractField
import penta.logic.field.ConnectionField
import penta.logic.field.StartField
import penta.redux_rewrite.BoardState

@Deprecated("move code away")
object PentaViz {
    private val logger = KotlinLogging.logger {}

    private val pieces: MutableMap<String, Pair<CircleNode, PathNode?>> = mutableMapOf()
    private val fieldElements = mutableMapOf<AbstractField, Triple<CircleNode, TextNode, TextNode?>>()
    private var playerCorners: List<PlayerCorner> = listOf()
    private lateinit var currentPlayerMarker: CircleNode
    private var scale: Double = 100.0
    private var mousePos: Point = Point(0.0, 0.0)
//    val turnDisplay: AtomicRef<String> = StandardObservableProperty("")
//    val gameStateProperty: StandardObservableProperty<ClientGameState?> = StandardObservableProperty(null)
    lateinit var gameState: ClientGameState
    var multiplayerState: ConnectionState = ConnectionState.Disconnected()
    lateinit var centerDisplay: Pair<CircleNode, TextNode>

    fun highlightedPieceAt(mousePos: Point): Piece? = gameState.findPiecesAtPos(mousePos).firstOrNull()?.let {
        val boardState = gameState.boardState
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

    val viz = viz {
        logger.info { ("height: $height") }
        logger.info { ("width: $width") }
//        turnDisplay = StandardObservableProperty("")
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
                if (field is StartField) {
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
            fieldElements[field] = Triple(c, t1, t2)
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

            fieldElements.forEach { (field, triple) ->
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

            gameState.boardState.figures.forEach {
                updatePiece(it, gameState.boardState)
            }
        }
    }

    init {
        gameState.let { gameState ->
            logger.info { "setting gameState" }
            logger.info { "resetting" }
            gameState!!.updatePiece = PentaViz::updatePiece

            viz.apply {
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

                playerCorners = gameState!!.boardState.players.map {
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
                if (PentaViz::currentPlayerMarker.isInitialized) {
                    currentPlayerMarker.remove()
                }
                currentPlayerMarker = circle {
                    stroke = 0.col
                    strokeWidth = 3.0
                }

                // init pieces
                gameState.boardState.figures.forEach { piece ->
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

                    updatePiece(piece, gameState.boardState)
                }
                updateBoard(render = false)
            }

            logger.info { "reset complete" }
        }
    }

    fun updateCorners() {
        val boardState = gameState.boardState
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

                drawFigure(figureId = corner.player.figureId, center = facePos, radius = pieceRadius)
            }

            // TODO: update place of nodes
        }
    }

    fun updatePlayers() {
        val boardState = gameState.boardState
        logger.info { "updating player render" }
        viz.apply {
            playerCorners.forEach { corner ->
                corner.face.remove()
                corner.graySlot.remove()
            }
            // get all player pieces
//            logger.info { gameStateProperty }
            val playerFigures = boardState.figures.filterIsInstance<Piece.Player>()
            playerFigures.forEach { figure ->
                val (circle, path) = pieces[figure.id] ?: return@forEach
                circle.remove()
                path?.remove()
                pieces.remove(figure.id)
            }

            playerCorners = boardState.players.map {
                logger.debug { ("init face $it") }
                PlayerCorner(
                    it,
                    path {},
                    circle {
                        visible = false
                        fill = Colors.Web.lightgrey.brighten(0.5)
                        stroke = 0.col
                        strokeWidth = 1.0
                    }
                )
            }

            if (PentaViz::currentPlayerMarker.isInitialized) {
                currentPlayerMarker.remove()
            }
            currentPlayerMarker = circle {
                stroke = 0.col
                strokeWidth = 3.0
            }

            // init pieces
            playerFigures.forEach { piece ->
                logger.debug { ("initialzing piece: $piece") }
                val c = circle {
                    strokeWidth = 4.0
                    stroke = piece.color
                }

                val p = path {
                    vAlign = TextVAlign.MIDDLE
                    hAlign = TextHAlign.MIDDLE

                    strokeWidth = 2.0
                    stroke = Colors.Web.black
                }


                pieces[piece.id] = Pair(c, p)

                updatePiece(piece, gameState.boardState)
            }
            updateBoard()
        }
    }

    fun recolor() {
        val highlightedPiece = highlightedPieceAt(mousePos)
        val highlightedField = if (highlightedPiece == null) PentaBoard.findFieldAtPos(mousePos) else null
        fieldElements.forEach { (field, triple) ->
            val (circle, text1, text2) = triple
            with(circle) {
                val c = if (field == highlightedField && gameState.canClickField(field))
                    field.color//.brighten(2.0)
                else
                    field.color

                if (field is StartField) {
                    stroke = c
                } else {
                    fill = c
                }
            }
        }
        val boardState = gameState.boardState
        boardState.figures.forEach {
            updatePiece(it, boardState)
        }
    }



    fun updateBoard(render: Boolean = true) {
        val boardState = gameState.boardState
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
        if (render) {
            viz.render()
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
        var pos: Point = field?.pos ?: run {
            val radius = when (piece) {
                is Piece.GrayBlocker -> {
                    logger.info{"piece: ${piece.id}"}
                        logger.info{"selected: ${selectedGrayPiece?.id}"}
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
                        logger.info{"cornerPos: $pos"}
                        return@run pos
                    }
                    throw kotlin.IllegalStateException("black piece: $piece cannot be off the board")
                }
                is Piece.Player -> PentaMath.inner_r * -0.5
//                else -> throw NotImplementedError("unhandled piece type: ${piece::class}")
            }
            val angle = (piece.pentaColor.ordinal * -72.0).deg

            logger.info{"pentaColor: ${piece.pentaColor.ordinal}"}

            io.data2viz.geom.Point(
                radius * angle.cos,
                radius * angle.sin
            ) / 2 + (io.data2viz.geom.Point(0.5, 0.5) * PentaMath.R_)
        }
        if (piece is Piece.Player && field is StartField) {
            // find all pieces on field and order them
            val pieceIds: List<String> = positions.filterValues { it == field }.keys
                .sorted()
            // find index of piece on field
            val pieceNumber = pieceIds.indexOf(piece.id).toDouble()
            val angle =
                (((field.pentaColor.ordinal * -72.0) + (pieceNumber / pieceIds.size * 360.0) + 360.0) % 360.0).deg
            pos = io.data2viz.geom.Point(
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
            pos = io.data2viz.geom.Point(
                pos.x + (0.55) * angle.cos,
                pos.y + (0.55) * angle.sin
            )
        }
        pos
    }
    fun updatePiece(piece: Piece, boardState: BoardState) {
//        val boardState = gameState.boardStore.state
        val highlightedPiece = highlightedPieceAt(mousePos)

        val (circle, path) = pieces[piece.id] ?: throw IllegalArgumentException("piece; $piece is not on the board")

        // TODO: highlight player pieces on turn when not placing black or gray

        val pos = calculatePiecePos(piece, boardState.positions[piece.id], boardState)
        val fillColor = when (piece) {
            is Piece.Player -> {
                if (
                    boardState.selectedPlayerPiece == null
                    && boardState.currentPlayer.id == piece.playerId
                    && canClickPiece(piece, boardState)
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
            drawFigure(figureId = playerPiece.figureId, center = Point(x = x, y = y), radius = maxRadius)
            fill = fillColor
            stroke = circle.stroke
        }
    }

    var hoveredField: AbstractField? = null
    var hoveredPiece: Piece? = null

    fun Viz.addEvents() {
        on(KPointerMove) { evt ->
            val boardState = gameState.boardState

            // convert pos back
            mousePos = (evt.pos / scale) * PentaMath.R_

            // show text on hovered fields
            PentaBoard.findFieldAtPos(mousePos).let { hoverField ->
                fieldElements.forEach { (field, triple) ->
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
//                    when (val state = multiplayerState.value) {
//                        is ConnectionState.HasGameSession -> {
//                            if (boardState.currentPlayer.id != state.userId) {
//                                return@let null
//                            }
//                        }
//                    }
                    if (
                        (piece !is Piece.Player)
                        || (piece.playerId != boardState.currentPlayer.id)
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
                        boardState.selectedPlayerPiece == null
                        && boardState.selectedGrayPiece == null
                        && boardState.selectedBlackPiece == null
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