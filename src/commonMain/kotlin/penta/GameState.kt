package penta

import PentaBoard
import PentaMath
import PentaViz
import io.data2viz.geom.Point
import io.data2viz.math.deg
import penta.field.AbstractField
import penta.field.CornerField
import penta.field.JointField
import penta.figure.BlackBlockerPiece
import penta.figure.GrayBlockerPiece
import penta.figure.Piece
import penta.figure.PlayerPiece
import penta.math.length

data class GameState(
    // player ids
    val players: List<String>,
    // player id to team id
    val teams: Map<String, Int>
) {
    var updatePiece: (Piece) -> Unit = { piece -> }

    private var turn: Int = 0
    val currentPlayer: String
        get() = if(players.isNotEmpty()) players[turn % players.count()] else "nobody"

    var selectedPlayerPiece: PlayerPiece? = null
    var selectedBlackPiece: BlackBlockerPiece? = null
    var selectedGrayPiece: GrayBlockerPiece? = null

    /**
     * true when no gray pieces are in the middle and one from the board can be selected
     */
    var selectingGrayPiece: Boolean = false

    // TODO: add figure registry
    val figures: Array<Piece>
    private val positions: MutableMap<String, AbstractField?> = mutableMapOf()
    val figurePositions: Map<String, AbstractField?> get() = positions

    fun updatePiecePos(piece: Piece) {
        val field: AbstractField? = positions[piece.id]
        var pos: Point = field?.pos ?: run {
            val radius = when (piece) {
                is GrayBlockerPiece -> PentaMath.inner_r * -0.25
                is BlackBlockerPiece -> throw IllegalStateException("black piece: $piece cannot be off the board")
                is PlayerPiece -> PentaMath.inner_r * -0.5
                else -> throw NotImplementedError("unhandled piece type: ${piece::class}")
            }
            val angle = (piece.pentaColor.ordinal * -72.0).deg

            println("pentaColor: ${piece.pentaColor.ordinal}")

//            val radius = (PentaMath.inner_r / PentaMath.R_) * scale
            Point(
                radius * angle.cos,
                radius * angle.sin
            ).also {
                println(it)
            } / 2 + (Point(0.5, 0.5) * PentaMath.R_)
        }
        if (piece is PlayerPiece && field is CornerField) {
            // TODO: find all pieces on field and order them
            val pieceIds: List<String> = positions.filterValues { it == field }.keys
                .sorted()
            // TODO: find index of piece on field
            val pieceNumber = pieceIds.indexOf(piece.id).toDouble()
            val playerIndex = players.indexOf(piece.playerId).toDouble()
//            val angle = (((piece.pentaColor.ordinal * -72.0) + (playerIndex / players.size * 360.0)+360.0) % 360.0).deg
            val angle =
                (((field.pentaColor.ordinal * 72.0) + (pieceNumber / pieceIds.size * 360.0) + 360.0) % 360.0).deg
//            val halfCircleWidth = (1.0 / PentaMath.R_) * scale / 2
            pos = Point(
                pos.x + (0.5) * angle.cos,
                pos.y + (0.5) * angle.sin
            )
        }
        if (piece is PlayerPiece && field == null) {
            // TODO: find all pieces on field and order them
            val playerPieces = positions.filterValues { it == field }.keys
                .map { id -> figures.find { it.id == id }!! }
                .filterIsInstance<PlayerPiece>()
                .filter { it.pentaColor == piece.pentaColor }
                .sortedBy { it.id }
            // TODO: find index of piece on field
            val pieceNumber = playerPieces.indexOf(piece).toDouble()
            val angle =
                (((piece.pentaColor.ordinal * -72.0) + (pieceNumber / playerPieces.size * 360.0) + 360.0) % 360.0).deg
            pos = Point(
                pos.x + (0.4) * angle.cos,
                pos.y + (0.4) * angle.sin
            )
        }
        piece.pos = pos
        updatePiece(piece)
    }

    // init figures and positions
    init {
        val blacks = (0 until 5).map { i ->
            BlackBlockerPiece(
                "b$i",
                Point(0.0, 0.0),
                PentaMath.s / 2.5 / 2,
                PentaColor.values()[i]
            ).also {
                positions[it.id] = PentaBoard.j[i]
            }
        }
        val greys = (0 until 5).map { i ->
            GrayBlockerPiece(
                "g$i",
                Point(0.0, 0.0),
                PentaMath.s / 2.5 / 2,
                PentaColor.values()[i]
            ).also {
                positions[it.id] = null
            }
        }
        val players = (0 until players.size).flatMap { p ->
            (0 until 5).map { i ->
                PlayerPiece(
                    "p$p$i",
                    players[p],
                    Point(0.0, 0.0),
                    PentaMath.s / 1.5 / 2,
                    PentaColor.values()[i]
                ).also {
                    positions[it.id] = PentaBoard.c[i]
                }
            }
        }
        figures = (blacks + greys + players).toTypedArray()
        figures.forEach(::updatePiecePos)
    }

    fun findPiecesAtPos(mousePos: Point) = figures.filter {
        (it.pos - mousePos).length < it.radius
    }

    fun canClickPiece(clickedPiece: Piece): Boolean {
        if(positions[clickedPiece.id] == null) {
            return false
        }
        if(
            // make sure you are not selecting black or gray
            selectedGrayPiece == null && selectedBlackPiece == null && !selectingGrayPiece
            && clickedPiece is PlayerPiece && currentPlayer == clickedPiece.playerId
        ) {
            if (selectedPlayerPiece == null) {
                return true
            }
            if (selectedPlayerPiece == clickedPiece) {
                return true
            }
        }

        if (selectingGrayPiece
            && selectedPlayerPiece == null
            && clickedPiece is GrayBlockerPiece
        ) {
            return true
        }

        if (selectedPlayerPiece != null && currentPlayer == selectedPlayerPiece!!.playerId) {
            val playerPiece = selectedPlayerPiece!!
            val sourcePos = positions[playerPiece.id] ?: run {
                return false
            }
            val targetPos = positions[clickedPiece.id] ?: return false
            if (sourcePos == targetPos) {
                return false
            }
            return true
        }
        return false
    }

    fun clickPiece(clickedPiece: Piece) {
        // TODO: check turn
        println("currentPlayer: $currentPlayer")
        println("selected player piece: $selectedPlayerPiece")
        println("selected black piece: $selectedBlackPiece")
        println("selected gray piece: $selectedGrayPiece")

        if(!canClickPiece(clickedPiece)) return

        if(positions[clickedPiece.id] == null) {
            println("cannot click piece off the board")
            return
        }
        if(
        // make sure you are not selecting black or gray
            selectedGrayPiece == null && selectedBlackPiece == null && !selectingGrayPiece
            && clickedPiece is PlayerPiece && currentPlayer == clickedPiece.playerId
        ) {
            if (selectedPlayerPiece == null) {
                println("selecting: $clickedPiece")
                selectedPlayerPiece = clickedPiece
                PentaViz.viz.render()
                return
            }
            if (selectedPlayerPiece == clickedPiece) {
                println("deselecting: $clickedPiece")
                selectedPlayerPiece = null
                PentaViz.viz.render()
                return
            }
        }

        if (selectingGrayPiece
            && selectedPlayerPiece == null
            && clickedPiece is GrayBlockerPiece
        ) {
            println("selecting: $clickedPiece")
            selectedGrayPiece = clickedPiece
            selectingGrayPiece = false
            PentaViz.viz.render()
            return
        }
        if (selectedPlayerPiece != null && currentPlayer == selectedPlayerPiece!!.playerId) {
            val playerPiece = selectedPlayerPiece!!
            // TODO: doMove
            val sourceField = positions[playerPiece.id] ?: run {
                println("piece if off the board already")
                return
            }
            val targetField = positions[clickedPiece.id]
            if (targetField == null) {
                println("$clickedPiece is not on the board")
//                selectedPlayerPiece = null
                return
            }
            if (sourceField == targetField) {
                println("cannot move piece onto the same field as before")
                return
            }

            println("swapping $sourceField <-> $targetField")

            if(!canMove(sourceField, targetField)) {
                println("can not find path")
                return
            }

            // swap pieces

            println("moving: ${playerPiece.id} -> $targetField")
            positions[playerPiece.id] = targetField
            // TODO movePiece(...) -> set position, update source pos, update target fields
            updatePiecesAtPos(sourceField)
            updatePiecesAtPos(targetField)
            selectedPlayerPiece = null

            when (clickedPiece) {
                is PlayerPiece -> {

                    if (sourceField is JointField && sourceField.pentaColor == clickedPiece.pentaColor) {
                        // take piece off the board
                        positions[clickedPiece.id] = null
                        updatePiecesAtPos(targetField)
//                        updatePiecePos(clickedPiece)
                        updatePiecesAtPos(null)

                        // other player moved this player into the joinfield, do we still let them set a grey
                        // this will overcomplicate things
                        // TODO: set gamestate to `MOVE_GREY`
                    } else {
                        // move normally
                        println("moving: ${clickedPiece.id} -> ${sourceField.id}")
                        positions[clickedPiece.id] = sourceField
                        updatePiecesAtPos(targetField)
                        updatePiecesAtPos(sourceField)
                    }
                    // TODO: also check if this is the matching JointField for the other piece?
                    // and leave board
                }
                is GrayBlockerPiece -> {
                    println("taking ${clickedPiece.id} off the board")
                    positions[clickedPiece.id] = null
                    updatePiecePos(clickedPiece)
                }
                is BlackBlockerPiece -> {
                    selectedBlackPiece = clickedPiece

                    // TODO: set gamestate to `MOVE_BLACK`
                    // TODO: implement moving black piece
                    // TODO("implement moving black piece")

                    // temporary
//                    positions[clickedPiece.id] = sourcePos
//                    updatePiecesAtPos(sourcePos)

                    selectedBlackPiece = clickedPiece
//                    updatePiecesAtPos(targetPos)
                }
            }

            // CHECK if targetPos is the target field for the clickedPiece

            if (targetField is JointField && targetField.pentaColor == playerPiece.pentaColor) {
                // take piece off the board
                positions[playerPiece.id] = null
                updatePiecesAtPos(null)
//                updatePiecePos(playerPiece)
                // set gamestate to `MOVE_GREY`
                selectedGrayPiece = positions.filterValues { it == null }
                    .keys.map { id -> figures.find { it.id == id } }
                    .filterIsInstance<GrayBlockerPiece>()
                    .firstOrNull()
                if (selectedGrayPiece == null) {
                    selectingGrayPiece = true
                }
            }

            // do not increase turn when placing grey or black
            if (selectedBlackPiece == null && selectedGrayPiece == null && !selectingGrayPiece) {
                turn += 1
            }
            PentaViz.viz.render()
            return
        }
        println("no action on click")
    }

    fun canClickField(targetField: AbstractField): Boolean {
        if(
            (selectedPlayerPiece == null && selectedGrayPiece == null && selectedBlackPiece == null)
            && positions.none { (k,v) -> v == targetField }
        ) {
            return false
        }
        when {
            selectedPlayerPiece != null && currentPlayer == selectedPlayerPiece!!.playerId -> {
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
//                    println("target position not empty")
                    return false
                }
            }
            selectedGrayPiece != null -> {
                if (positions.values.any { it == targetField }) {
//                    println("target position not empty")
                    return false
                }
            }
            selectedPlayerPiece == null && selectedBlackPiece == null && selectedGrayPiece == null -> {
                // do not allow clicking on field when selecting piece
                return false
            }
        }
        return true
    }

    fun clickField(targetField: AbstractField) {
        println("currentPlayer: $currentPlayer")
        println("selected player piece: $selectedPlayerPiece")
        println("selected black piece: $selectedBlackPiece")
        println("selected gray piece: $selectedGrayPiece")
        if(!canClickField(targetField)) return
        when {
            selectedPlayerPiece != null && currentPlayer == selectedPlayerPiece!!.playerId -> {
                val playerPiece = selectedPlayerPiece!!


                val sourceField = positions[playerPiece.id]!!
                if (sourceField == targetField) {
                    println("cannot move piece onto the same field as before")
                    return
                }

                // check if targetField is empty
                if (positions.values.any { it == targetField }) {
                    println("target position not empty")
                    // TODO: if there is only one piece on the field, click that piece instead ?
                    val pieces = positions.filterValues { it == targetField }.keys
                        .map { id ->
                            figures.find { it.id == id }
                        }
                    if(pieces.size == 1) {
                        val piece = pieces.firstOrNull() ?: return
                        clickPiece(piece)
                    }
                    return
                }

                if(!canMove(sourceField, targetField)) {
                    println("can not find path")
                    return
                }
                // TODO: check if move is possible
                // swap pieces

                println("moving: ${playerPiece.id} -> $targetField")
                positions[playerPiece.id] = targetField
                // TODO movePiece(...) -> set position, update source pos, update target fields
//                updatePiecesAtPos(sourcePos)
//                updatePiecesAtPos(targetField)
                selectedPlayerPiece = null

                if (targetField is JointField && targetField.pentaColor == playerPiece.pentaColor) {
                    positions[playerPiece.id] = null
//                    updatePiecesAtPos(null)

                    // set gamestate to `MOVE_GREY`
                    selectedGrayPiece = positions.filterValues { it == null }
                        .keys.map { id -> figures.find { it.id == id } }
                        .filterIsInstance<GrayBlockerPiece>()
                        .firstOrNull()
                    if (selectedGrayPiece == null) {
                        selectingGrayPiece = true
                    }
                }
            }
            selectedBlackPiece != null -> {
                val blackPiece = selectedBlackPiece!!

                if (positions.values.any { it == targetField }) {
                    println("target position not empty")
                    return
                }

                positions[blackPiece.id] = targetField
//                updatePiecesAtPos(targetField)
                selectedBlackPiece = null

            }
            selectedGrayPiece != null -> {
                val grayPiece = selectedGrayPiece!!

                if (positions.values.any { it == targetField }) {
                    println("target position not empty")
                    return
                }

                positions[grayPiece.id] = targetField
//                updatePiecesAtPos(targetField)
                selectedGrayPiece = null
            }
        }
        // do not increase turn when placing grey or black
        if (selectedBlackPiece == null && selectedGrayPiece == null && !selectingGrayPiece) {
            turn += 1
        }
        updateAllPieces()
        PentaViz.viz.render()
    }

    private fun updateAllPieces() {
        figures.forEach { piece ->
            updatePiecePos(piece)
        }
    }

    private fun updatePiecesAtPos(field: AbstractField?) {
        positions.filterValues { it == field }.keys.map { id ->
            figures.find { it.id == id }!!
        }.forEach { piece ->
            updatePiecePos(piece)
        }
    }

    fun canMove(start: AbstractField, end: AbstractField): Boolean {
        val backtrack = mutableSetOf<AbstractField>()
        val next = mutableSetOf<AbstractField>(start)
        while(next.isNotEmpty()) {
            val toIterate = next.toList()
            next.clear()
            toIterate.map { nextField ->
                println("checking: ${nextField.id}")
                nextField.connected.forEach { connectedField ->
                    if(connectedField == end) return true
                    if(connectedField in backtrack) return@forEach

                    val free = positions.filterValues { field ->
                        field == connectedField
                    }.isEmpty()
                    if(!free) return@forEach

                    if(connectedField !in backtrack) {
                        backtrack += connectedField
                        next += connectedField
                    }
                }
            }
        }

        return false
    }
}