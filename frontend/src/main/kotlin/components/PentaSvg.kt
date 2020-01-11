package components

import PentaBoard
import PentaMath
import com.github.nwillc.ksvg.elements.SVG
import containers.PentaSvgDispatchProps
import containers.PentaSvgStateProps
import io.data2viz.geom.Point
import io.data2viz.math.Angle
import io.data2viz.math.deg
import mu.KotlinLogging
import org.w3c.dom.Element
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.svg.SVGCircleElement
import org.w3c.dom.svg.SVGElement
import penta.*
import penta.logic.Piece
import penta.logic.field.AbstractField
import penta.logic.field.ConnectionField
import penta.logic.field.GoalField
import penta.logic.field.StartField
import react.RBuilder
import react.RComponent
import react.RState
import react.createRef
import react.dom.svg
import kotlin.browser.document
import kotlin.dom.clear

interface PentaSvgProps : PentaSvgStateProps, PentaSvgDispatchProps {
//    var boardState: BoardState
//    var addPlayerClick: () -> Unit
//    var startGameClick: () -> Unit
}

class PentaSvg(props: PentaSvgProps) : RComponent<PentaSvgProps, RState>(props) {
    private val svgRef = createRef<SVGElement>()
    override fun RBuilder.render() {
        svg {
            ref = svgRef
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun componentDidMount() {
        console.log("penta svg mounted")

        redraw(props)
    }

    override fun shouldComponentUpdate(nextProps: PentaSvgProps, nextState: RState): Boolean {
        // TODO update svg content here
        console.log("penta svg updating")
        console.log("props: ${props.boardState}")
        console.log("nextProps: ${nextProps.boardState}")

        redraw(nextProps)

        return false
    }

    private fun redraw(svgProps: PentaSvgProps) {
        console.log("drawing...")

        // does SVG stuff
        svgRef.current?.let { svg ->
            svg.clear()

            val scale = 1000

            fun Double.s() = toInt().toString()

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
                            clickField(PentaBoard.fields.first { it.id == target.id }, svgProps)
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
                        clickPiece(this.props.boardState.figures.first { it.id == target.id }, svgProps)
                    }
                }
            }
        }

        PentaBoard.fields.forEach {
            document.getElementById(it.id)?.addEventListener("click", clickFields, true)
        }
        props.boardState.figures.forEach {
//            console.log("registering $it")
            document.getElementById(it.id)?.let { el ->
//                console.log("registering onclick for ${it.id} $el")
                el.addEventListener("click", clickPieces, true)
            }
        }
    }
}

    private fun SVG.draw(scale: Int, svgProps: PentaSvgProps) {
//        val lineWidth = (PentaMath.s / 5) / PentaMath.R_ * scale
        val lineWidth = 0.1 / PentaMath.R_ * scale

        console.log("lineWidth: $lineWidth")
//        console.log("thinLineWidth: $thinLineWidth")

//        style {
//            body = """
//             svg .black-stroke { stroke: black; stroke-width: 2; }
//            """.trimIndent()
//        }

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
                        id = field.id
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
                        id = field.id
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
                        id = field.id
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

        svgProps.boardState.figures.forEach { piece ->
            val pos = updatePiecePos(piece, svgProps.boardState.positions[piece.id], svgProps)
            val field = svgProps.boardState.positions[piece.id]
            console.log("drawing piece ${piece.id} on field $field")
            val scaledPos = pos / PentaMath.R_ * scale
            val radius = (piece.radius / PentaMath.R_ * scale)
            when (piece) {
                is Piece.GrayBlocker -> {
                    circle {
                        id = piece.id
                        cx = "${scaledPos.x}"
                        cy = "${scaledPos.y}"
                        r = "$radius"
                        fill = piece.color.rgbHex
                    }
                }
                is Piece.BlackBlocker -> {
                    circle {
                        id = piece.id
                        cx = "${scaledPos.x}"
                        cy = "${scaledPos.y}"

                        r = "$radius"
                        fill = piece.color.rgbHex
                    }
                }
                is Piece.Player -> {
                    console.log("playerPiece: $piece")
                    drawPlayer(piece.figureId, scaledPos, radius, piece, svgProps.boardState.selectedPlayerPiece == piece)
//                    circle {
//                        id = piece.id
//                        cx = "${scaledPos.x}"
//                        cy = "${scaledPos.y}"
//
//                        r = "$radius"
//
//                        fill = piece.color.rgbHex
//                    }
                }
            }
        }

        // TODO: add corner fields

        // TODO add other into
    }

    fun cornerPoint(index: Int, angleDelta: Angle = 0.deg, radius: Double = PentaMath.R_): Point {
        val angle = (-45 + (index) * 90).deg + angleDelta

        return Point(
            radius * angle.cos,
            radius * angle.sin
        ) / 2 + (Point(0.5, 0.5) * PentaMath.R_)
    }

    fun updatePiecePos(piece: Piece, field: AbstractField?, svgProps: PentaSvgProps) = with(svgProps.boardState) {
        var pos: Point = field?.pos ?: run {
            val radius = when (piece) {
                is Piece.GrayBlocker -> {
                    console.log("piece: ${piece.id}")
                    console.log("selected: ${selectedGrayPiece?.id}")
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
                        console.log("cornerPos: $pos")
                        return@run pos
                    }
                    throw kotlin.IllegalStateException("black piece: $piece cannot be off the board")
                }
                is Piece.Player -> PentaMath.inner_r * -0.5
//                else -> throw NotImplementedError("unhandled piece type: ${piece::class}")
            }
            val angle = (piece.pentaColor.ordinal * -72.0).deg

            console.log("pentaColor: ${piece.pentaColor.ordinal}")

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
        piece.pos = pos
        pos
    }

    fun preProcessMove(move: PentaMove, svgProps: PentaSvgProps) {
        console.info("preProcess $move")
        // TODO: check connection state
//        when (val state = PentaViz.multiplayerState) {
//            is ConnectionState.Observing -> {
//                GlobalScope.launch(Dispatchers.Default) {
//                    state.sendMove(move)
//                }
//            }
//            else -> {
//                boardStoreDispatch(move)
////                boardStore.dispatch(move)
//                boardState.illegalMove?.let { illegalMove ->
//                    ClientGameState.logger.error { "TODO: add popup about $illegalMove" }
//                }
//            }
//        }
        svgProps.dispatch(move)
        svgProps.boardState.illegalMove?.let { illegalMove ->
            console.error("TODO: add popup about $illegalMove")
        }
        // TODO: if playing online.. send move
        // only process received moves
    }

    /**
     * click on a piece
     * @param clickedPiece game piece that was clicked on
     */
    fun clickPiece(clickedPiece: Piece, svgProps: PentaSvgProps) = with(svgProps.boardState) {
        if (!canClickPiece(clickedPiece, svgProps.boardState)) return

        console.info("currentPlayer: $currentPlayer")
        console.info("selected player piece: $selectedPlayerPiece")
        console.info("selected black piece: $selectedBlackPiece")
        console.info("selected gray piece: $selectedGrayPiece")

        if (positions[clickedPiece.id] == null) {
            console.error("cannot click piece off the board")
            return
        }
        if (
        // make sure you are not selecting black or gray
            selectedGrayPiece == null && selectedBlackPiece == null && !selectingGrayPiece
            && clickedPiece is Piece.Player && currentPlayer.id == clickedPiece.playerId
        ) {
            if (selectedPlayerPiece == null) {
                console.info("selecting: $clickedPiece")
                // TODO: boardStateStore.dispatch(...)
                svgProps.dispatch(PentaMove.SelectPlayerPiece(clickedPiece))
//                boardStore.dispatch(PentaMove.SelectPlayerPiece(clickedPiece))
//                selectedPlayerPiece = clickedPieceg
//                client.PentaViz.updateBoard()
                return
            }
            if (selectedPlayerPiece == clickedPiece) {
                console.info("deselecting: $clickedPiece")

                svgProps.dispatch(PentaMove.SelectGrey(null))
//                boardStore.dispatch(PentaMove.SelectGrey(null))
//                selectedPlayerPiece = null
//                client.PentaViz.updateBoard()
                return
            }
        }

        if (selectingGrayPiece
            && selectedPlayerPiece == null
            && clickedPiece is Piece.GrayBlocker
        ) {
            console.info("selecting: $clickedPiece")
            // TODO: boardStateStore.dispatch(...)
            svgProps.dispatch(PentaMove.SelectGrey(clickedPiece))
//            boardStore.C(PentaMove.SelectGrey(clickedPiece))
//            selectedGrayPiece = clickedPiece
//            selectingGrayPiece = false
//            clickedPiece.position = null
            updatePiecePos(clickedPiece, null, svgProps)
//            client.PentaViz.updateBoard()
            return
        }
        if (selectedPlayerPiece != null && currentPlayer.id == selectedPlayerPiece!!.playerId) {
            val playerPiece = selectedPlayerPiece!!
            val sourceField = positions[playerPiece.id] ?: run {
                console.error("piece if off the board already")
                return
            }
            val targetField = positions[clickedPiece.id]
            if (targetField == null) {
                console.error("$clickedPiece is not on the board")
//                selectedPlayerPiece = null
                return
            }
            if (sourceField == targetField) {
                console.error("cannot move piece onto the same field as before")
                return
            }

            if (!canMove(sourceField, targetField)) {
                console.error("can not find path")
                return
            }

            console.info("moving: ${playerPiece.id} -> $targetField")

            val move: PentaMove = when (clickedPiece) {
                is Piece.Player -> {
                    if (playerPiece.playerId == clickedPiece.playerId) {
                        PentaMove.SwapOwnPiece(
                            playerPiece = playerPiece, otherPlayerPiece = clickedPiece,
                            from = sourceField, to = targetField
                        )
                    } else {
                        // TODO   if (player is in your team) {
                        PentaMove.SwapHostilePieces(
                            playerPiece = playerPiece, otherPlayerPiece = clickedPiece,
                            from = sourceField, to = targetField
                        )
                    }
                }
                is Piece.GrayBlocker -> {
                    PentaMove.MovePlayer(
                        playerPiece = playerPiece, from = sourceField, to = targetField
                    )
                }
                is Piece.BlackBlocker -> {
                    PentaMove.MovePlayer(
                        playerPiece = playerPiece, from = sourceField, to = targetField
                    )
                }
            }
            preProcessMove(move, svgProps)

            return
        }
        console.info("no action on click")
    }

    fun canClickField(targetField: AbstractField, svgProps: PentaSvgProps): Boolean {
        with(svgProps.boardState) {
            if (winner != null) {
                return false
            }
            if (
                (selectedPlayerPiece == null && selectedGrayPiece == null && selectedBlackPiece == null)
                && positions.none { (k, v) -> v == targetField }
            ) {
                return false
            }
//            when (val state = client.PentaViz.multiplayerState) {
//                is ConnectionState.HasGameSession -> {
//                    if (currentPlayer.id != state.userId) {
//                        return false
//                    }
//                }
//            }
            when {
                selectedPlayerPiece != null && currentPlayer.id == selectedPlayerPiece!!.playerId -> {
                    val playerPiece = selectedPlayerPiece!!

                    val sourcePos = positions[playerPiece.id]!!
                    if (sourcePos == targetField) {
                        return false
                    }

                    // check if targetField is empty
                    if (positions.values.any { it == targetField }) {
                        val pieces = positions.filterValues { it == targetField }.keys
                            .map { id ->
                                figures.find { it.id == id }
                            }
                        pieces.firstOrNull() ?: return false
                        return true
                    }
                }
                selectedBlackPiece != null -> {
                    if (positions.values.any { it == targetField }) {
                        console.info("target position not empty")
                        return false
                    }
                }
                selectedGrayPiece != null -> {
                    if (positions.values.any { it == targetField }) {
                        console.info("target position not empty")
                        return false
                    }
                }
                selectedPlayerPiece == null && selectedBlackPiece == null && selectedGrayPiece == null -> {
                    // do not allow clicking on field when selecting piece
                    return false
                }
            }
        }

        return true
    }

    fun clickField(targetField: AbstractField, svgProps: PentaSvgProps) = with(svgProps.boardState) {
        if (!canClickField(targetField, svgProps)) return
        console.info("currentPlayer: $currentPlayer")
        console.info("selected player piece: $selectedPlayerPiece")
        console.info("selected black piece: $selectedBlackPiece")
        console.info("selected gray piece: $selectedGrayPiece")
        val move = when {
            selectedPlayerPiece != null && currentPlayer.id == selectedPlayerPiece!!.playerId -> {
                val playerPiece = selectedPlayerPiece!!

                val sourceField = positions[playerPiece.id]!!
                if (sourceField == targetField) {
                    console.error("cannot move piece onto the same field as before")
                    return
                }

                // check if targetField is empty
                if (positions.values.any { it == targetField }) {
                    console.info("target position not empty")
                    // TODO: if there is only one piece on the field, click that piece instead ?
                    val pieces = positions.filterValues { it == targetField }.keys
                        .map { id ->
                            figures.find { it.id == id }
                        }
                    if (pieces.size == 1) {
                        val piece = pieces.firstOrNull() ?: return
                        clickPiece(piece, svgProps)
                    }
                    return
                }

                if (!canMove(sourceField, targetField)) {
                    console.error("can not find path")
                    return
                }

                console.info("moving: ${playerPiece.id} -> $targetField")

                PentaMove.MovePlayer(
                    playerPiece = playerPiece, from = sourceField, to = targetField
                )
            }
            selectedBlackPiece != null -> {
                val blackPiece = selectedBlackPiece!!

                if (positions.values.any { it == targetField }) {
                    console.error("target position not empty")
                    return
                }
                console.info("history last: ${history.last()}")
                val lastMove = history.last() as PentaMove.Move
                if (lastMove !is PentaMove.CanSetBlack) {
                    console.error("last move cannot set black")
                    return
                }

                PentaMove.SetBlack(
                    piece = blackPiece, from = lastMove.to, to = targetField
                )
            }
            selectedGrayPiece != null -> {
                val grayPiece = selectedGrayPiece!!

                if (positions.values.any { it == targetField }) {
                    console.error("target position not empty")
                    return
                }
                val originPos = positions[grayPiece.id]

                PentaMove.SetGrey(
                    piece = grayPiece, from = originPos, to = targetField
                )
            }
            else -> {
                console.error("else case not handled")
                return
            }
        }
        preProcessMove(move, svgProps)
    }


