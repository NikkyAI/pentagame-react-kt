package penta

import PentaMath
import penta.client.PentaViz
import io.data2viz.geom.Point
import io.data2viz.math.Angle
import io.data2viz.math.deg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.reduxkotlin.Store
import org.reduxkotlin.applyMiddleware
import org.reduxkotlin.createStore
import penta.logic.Piece
import penta.logic.field.AbstractField
import penta.logic.field.StartField
import penta.redux_rewrite.BoardState
import penta.util.length

@Deprecated("move code away")
open class ClientGameState(localPlayerCount: Int = 1) : GameState() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val boardStore: Store<BoardState> = createStore(
        BoardState.reducer,
        BoardState.create(
            // TODO: remove default users
            listOf(PlayerState("alice", "cross"), PlayerState("bob", "triangle")),
            BoardState.GameType.TWO
        ),
        applyMiddleware(/*loggingMiddleware(logger)*/)
    )

    val boardState: BoardState = boardStore.state
    fun boardStoreDispatch(move: PentaMove) {
        boardStore.dispatch(move);
    }

    //    override var updateLogPanel: (String) -> Unit = {}
    var updatePiece: (Piece, BoardState) -> Unit = { piece, boardState -> }

    fun cornerPoint(index: Int, angleDelta: Angle = 0.deg, radius: Double = PentaMath.R_): Point {
        val angle = (-45 + (index) * 90).deg + angleDelta

        return Point(
            radius * angle.cos,
            radius * angle.sin
        ) / 2 + (Point(0.5, 0.5) * PentaMath.R_)
    }

    fun initialize(players: List<PlayerState>) {
        logger.info { "initializing with $players" }
        players.forEach {
            boardStoreDispatch(PentaMove.PlayerJoin(it))
//            boardStore.dispatch(PentaMove.PlayerJoin(it))
        }
        boardStoreDispatch(PentaMove.InitGame)
//        boardStore.dispatch(PentaMove.InitGame)
    }

    init {
        val localSymbols = listOf("triangle", "square", "cross", "circle")
        if (localPlayerCount > 0) {
            initialize(localSymbols.subList(0, localPlayerCount).map { PlayerState("local+" + it, it) })
        }

        boardState.figures.forEach(::updatePiecePos)
//        boardState.figures.forEach(::updatePiecePos)
    }

    fun preProcessMove(move: PentaMove) {
        logger.info { "preProcess $move" }
        when (val state = PentaViz.multiplayerState) {
            is ConnectionState.Observing -> {
                GlobalScope.launch(Dispatchers.Default) {
                    state.sendMove(move)
                }
            }
            else -> {
                boardStoreDispatch(move)
//                boardStore.dispatch(move)
                boardState.illegalMove?.let { illegalMove ->
                    logger.error { "TODO: add popup about $illegalMove" }
                }
            }
        }
        // TODO: if playing online.. send move
        // only process received moves
    }

    override fun updatePiecePos(piece: Piece) {
        val field: AbstractField? = boardState.positions[piece.id]
        updatePiecePos(piece, field)
    }

    fun updatePiecePos(piece: Piece, field: AbstractField?) = with(boardState){
        var pos: Point = field?.pos ?: run {
            val radius = when (piece) {
                is Piece.GrayBlocker -> {
                    logger.debug { "piece: ${piece.id}" }
                    logger.debug { "selected: ${selectedGrayPiece?.id}" }
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
                        logger.info { "cornerPos: $pos" }
                        return@run pos
                    }
                    throw IllegalStateException("black piece: $piece cannot be off the board")
                }
                is Piece.Player -> PentaMath.inner_r * -0.5
//                else -> throw NotImplementedError("unhandled piece type: ${piece::class}")
            }
            val angle = (piece.pentaColor.ordinal * -72.0).deg

            logger.info { "pentaColor: ${piece.pentaColor.ordinal}" }

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
        piece.pos = pos
        updatePiece(piece, boardState)
    }

    fun findPiecesAtPos(mousePos: Point) = boardState.figures.filter {
        (it.pos - mousePos).length < it.radius
    }

    /**
     * click on a piece
     * @param clickedPiece game piece that was clicked on
     */
    fun clickPiece(clickedPiece: Piece) = with(boardState){
        if (!canClickPiece(clickedPiece, boardState)) return

        logger.info { "currentPlayer: $currentPlayer" }
        logger.info { "selected player piece: $selectedPlayerPiece" }
        logger.info { "selected black piece: $selectedBlackPiece" }
        logger.info { "selected gray piece: $selectedGrayPiece" }

        if (positions[clickedPiece.id] == null) {
            logger.error { "cannot click piece off the board" }
            return
        }
        if (
        // make sure you are not selecting black or gray
            selectedGrayPiece == null && selectedBlackPiece == null && !selectingGrayPiece
            && clickedPiece is Piece.Player && currentPlayer.id == clickedPiece.playerId
        ) {
            if (selectedPlayerPiece == null) {
                logger.info { "selecting: $clickedPiece" }
                // TODO: boardStateStore.dispatch(...)
                boardStoreDispatch(PentaMove.SelectPlayerPiece(clickedPiece))
//                boardStore.dispatch(PentaMove.SelectPlayerPiece(clickedPiece))
//                selectedPlayerPiece = clickedPieceg
                PentaViz.updateBoard()
                return
            }
            if (selectedPlayerPiece == clickedPiece) {
                logger.info { "deselecting: $clickedPiece" }

                boardStoreDispatch(PentaMove.SelectGrey(null))
//                boardStore.dispatch(PentaMove.SelectGrey(null))
//                selectedPlayerPiece = null
                PentaViz.updateBoard()
                return
            }
        }

        if (selectingGrayPiece
            && selectedPlayerPiece == null
            && clickedPiece is Piece.GrayBlocker
        ) {
            logger.info { "selecting: $clickedPiece" }
            // TODO: boardStateStore.dispatch(...)
            boardStoreDispatch(PentaMove.SelectGrey(clickedPiece))
//            boardStore.C(PentaMove.SelectGrey(clickedPiece))
//            selectedGrayPiece = clickedPiece
//            selectingGrayPiece = false
//            clickedPiece.position = null
            updatePiecePos(clickedPiece, null)
            PentaViz.updateBoard()
            return
        }
        if (selectedPlayerPiece != null && currentPlayer.id == selectedPlayerPiece!!.playerId) {
            val playerPiece = selectedPlayerPiece!!
            val sourceField = positions[playerPiece.id] ?: run {
                logger.error { "piece if off the board already" }
                return
            }
            val targetField = positions[clickedPiece.id]
            if (targetField == null) {
                logger.error { ("$clickedPiece is not on the board") }
//                selectedPlayerPiece = null
                return
            }
            if (sourceField == targetField) {
                logger.error { ("cannot move piece onto the same field as before") }
                return
            }

            if (!canMove(sourceField, targetField)) {
                logger.error { ("can not find path") }
                return
            }

            logger.info { ("moving: ${playerPiece.id} -> $targetField") }

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
            preProcessMove(move)

            return
        }
        logger.info { ("no action on click") }
    }

    override fun updateBoard() {
        super.updateBoard()
        PentaViz.updateBoard()
    }

    fun canClickField(targetField: AbstractField): Boolean {
        with(boardState) {
            if (winner != null) {
                return false
            }
            if (
                (selectedPlayerPiece == null && selectedGrayPiece == null && selectedBlackPiece == null)
                && positions.none { (k, v) -> v == targetField }
            ) {
                return false
            }
            when (val state = PentaViz.multiplayerState) {
                is ConnectionState.HasGameSession -> {
                    if (currentPlayer.id != state.userId) {
                        return false
                    }
                }
            }
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
                        logger.trace { ("target position not empty") }
                        return false
                    }
                }
                selectedGrayPiece != null -> {
                    if (positions.values.any { it == targetField }) {
                        logger.trace { ("target position not empty") }
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

    fun clickField(targetField: AbstractField) = with(boardState) {
        if (!canClickField(targetField)) return
        logger.info { ("currentPlayer: $currentPlayer") }
        logger.info { ("selected player piece: $selectedPlayerPiece") }
        logger.info { ("selected black piece: $selectedBlackPiece") }
        logger.info { ("selected gray piece: $selectedGrayPiece") }
        val move = when {
            selectedPlayerPiece != null && currentPlayer.id == selectedPlayerPiece!!.playerId -> {
                val playerPiece = selectedPlayerPiece!!

                val sourceField = positions[playerPiece.id]!!
                if (sourceField == targetField) {
                    logger.error { ("cannot move piece onto the same field as before") }
                    return
                }

                // check if targetField is empty
                if (positions.values.any { it == targetField }) {
                    logger.info { ("target position not empty") }
                    // TODO: if there is only one piece on the field, click that piece instead ?
                    val pieces = positions.filterValues { it == targetField }.keys
                        .map { id ->
                            figures.find { it.id == id }
                        }
                    if (pieces.size == 1) {
                        val piece = pieces.firstOrNull() ?: return
                        clickPiece(piece)
                    }
                    return
                }

                if (!canMove(sourceField, targetField)) {
                    logger.error { ("can not find path") }
                    return
                }

                logger.info { ("moving: ${playerPiece.id} -> $targetField") }

                PentaMove.MovePlayer(
                    playerPiece = playerPiece, from = sourceField, to = targetField
                )
            }
            selectedBlackPiece != null -> {
                val blackPiece = selectedBlackPiece!!

                if (positions.values.any { it == targetField }) {
                    logger.error { ("target position not empty") }
                    return
                }
                logger.info { "history last: ${history.last()}" }
                val lastMove = history.last() as PentaMove.Move
                if (lastMove !is PentaMove.CanSetBlack) {
                    logger.error { ("last move cannot set black") }
                    return
                }

                PentaMove.SetBlack(
                    piece = blackPiece, from = lastMove.to, to = targetField
                )
            }
            selectedGrayPiece != null -> {
                val grayPiece = selectedGrayPiece!!

                if (positions.values.any { it == targetField }) {
                    logger.error { ("target position not empty") }
                    return
                }
                val originPos = positions[grayPiece.id]

                PentaMove.SetGrey(
                    piece = grayPiece, from = originPos, to = targetField
                )
            }
            else -> {
                TODO("handle else case")
            }
        }
        preProcessMove(move)
    }

    override fun resetPlayers() {
//        super.resetPlayers()
        PentaViz.updatePlayers()
    }

    // TODO: clientside
    fun updateAllPieces() = with(boardState) {
        figures.forEach { piece ->
            updatePiecePos(piece)
        }
    }

    // TODO: clientside
    override fun updatePiecesAtPos(field: AbstractField?)= with(boardState)  {
        positions.filterValues { it == field }.keys.map { id ->
            figures.find { it.id == id }
                ?: throw IllegalStateException("cannot find figure with id: $id in ${figures.map { it.id }}")
        }.forEach { piece ->
            updatePiecePos(piece)
        }
    }
}