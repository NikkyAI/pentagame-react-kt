package penta

import mu.KotlinLogging
import penta.logic.Piece
import penta.logic.field.AbstractField
import penta.redux_rewrite.BoardState

/**
 * determines what action to take when clicking on fields or pieces
 */
object PentagameClick {
    private val logger = KotlinLogging.logger {}

    fun preProcessMove(move: PentaMove, dispatch: (PentaMove) -> Unit, boardState: BoardState) {
        logger.info { "preProcess $move" }
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
        dispatch(move)
        boardState.illegalMove?.let { illegalMove ->
            logger.error { "TODO: add popup about $illegalMove" }
        }
        // TODO: if playing online.. send move
        // only process received moves
    }

    /**
     * click on a piece
     * @param clickedPiece game piece that was clicked on
     */
    fun clickPiece(clickedPiece: Piece, dispatch: (PentaMove) -> Unit, boardState: BoardState) = with(boardState) {
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
                dispatch(penta.PentaMove.SelectPlayerPiece(clickedPiece))
//                boardStore.dispatch(PentaMove.SelectPlayerPiece(clickedPiece))
//                selectedPlayerPiece = clickedPieceg
//                client.PentaViz.updateBoard()
                return
            }
            if (selectedPlayerPiece == clickedPiece) {
                logger.info { "deselecting: $clickedPiece" }

                dispatch(penta.PentaMove.SelectGrey(null))
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
            logger.info { "selecting: $clickedPiece" }
            // TODO: boardStateStore.dispatch(...)
            dispatch(penta.PentaMove.SelectGrey(clickedPiece))
//            boardStore.C(PentaMove.SelectGrey(clickedPiece))
//            selectedGrayPiece = clickedPiece
//            selectingGrayPiece = false
//            clickedPiece.position = null
//        updatePiecePos(clickedPiece, null, svgProps)
//            client.PentaViz.updateBoard()
            return
        }
        if (selectedPlayerPiece != null && currentPlayer.id == selectedPlayerPiece!!.playerId) {
            val playerPiece = selectedPlayerPiece!!
            val sourceField = positions[playerPiece.id] ?: run {
                logger.error{"piece if off the board already"}
                return
            }
            val targetField = positions[clickedPiece.id]
            if (targetField == null) {
                logger.error{"$clickedPiece is not on the board"}
//                selectedPlayerPiece = null
                return
            }
            if (sourceField == targetField) {
                logger.error {"cannot move piece onto the same field as before" }
                return
            }

            if (!canMove(sourceField, targetField)) {
                logger.error {"can not find path" }
                return
            }

            logger.info {"moving: ${playerPiece.id} -> $targetField"}

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
            preProcessMove(move, dispatch, boardState)

            return
        }
        logger.info {"no action on click"}
    }

    fun canClickField(targetField: AbstractField, dispatch: (PentaMove) -> Unit, boardState: BoardState): Boolean {
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
                        logger.info {"target position not empty"}
                        return false
                    }
                }
                selectedGrayPiece != null -> {
                    if (positions.values.any { it == targetField }) {
                        logger.info {"target position not empty"}
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

    fun clickField(targetField: AbstractField, dispatch: (PentaMove) -> Unit, boardState: BoardState) =
        with(boardState) {
            if (!canClickField(targetField, dispatch, boardState)) return
            logger.info {"currentPlayer: $currentPlayer"}
            logger.info {"selected player piece: $selectedPlayerPiece"}
            logger.info {"selected black piece: $selectedBlackPiece"}
            logger.info {"selected gray piece: $selectedGrayPiece"}
            val move = when {
                selectedPlayerPiece != null && currentPlayer.id == selectedPlayerPiece!!.playerId -> {
                    val playerPiece = selectedPlayerPiece!!

                    val sourceField = positions[playerPiece.id]!!
                    if (sourceField == targetField) {
                        logger.error {"cannot move piece onto the same field as before"}
                        return
                    }

                    // check if targetField is empty
                    if (positions.values.any { it == targetField }) {
                        logger.info {"target position not empty"}
                        // TODO: if there is only one piece on the field, click that piece instead ?
                        val pieces = positions.filterValues { it == targetField }.keys
                            .map { id ->
                                figures.find { it.id == id }
                            }
                        if (pieces.size == 1) {
                            val piece = pieces.firstOrNull() ?: return
                            clickPiece(piece, dispatch, boardState)
                        }
                        return
                    }

                    if (!canMove(sourceField, targetField)) {
                        logger.error {"can not find path"}
                        return
                    }

                    logger.info {"moving: ${playerPiece.id} -> $targetField"}

                    penta.PentaMove.MovePlayer(
                        playerPiece = playerPiece, from = sourceField, to = targetField
                    )
                }
                selectedBlackPiece != null -> {
                    val blackPiece = selectedBlackPiece!!

                    if (positions.values.any { it == targetField }) {
                        logger.error {"target position not empty"}
                        return
                    }
                    logger.info {"history last: ${history.last()}"}
                    val lastMove = history.last() as PentaMove.Move
                    if (lastMove !is PentaMove.CanSetBlack) {
                        logger.error {"last move cannot set black"}
                        return
                    }

                    penta.PentaMove.SetBlack(
                        piece = blackPiece, from = lastMove.to, to = targetField
                    )
                }
                selectedGrayPiece != null -> {
                    val grayPiece = selectedGrayPiece!!

                    if (positions.values.any { it == targetField }) {
                        logger.error {"target position not empty"}
                        return
                    }
                    val originPos = positions[grayPiece.id]

                    penta.PentaMove.SetGrey(
                        piece = grayPiece, from = originPos, to = targetField
                    )
                }
                else -> {
                    logger.error {"else case not handled"}
                    return
                }
            }
            preProcessMove(move, dispatch, boardState)
        }
}
