import io.data2viz.color.col
import io.data2viz.geom.Point
import io.data2viz.math.deg
import io.data2viz.scale.ScalesChromatic
import io.data2viz.viz.CircleNode
import io.data2viz.viz.KMouseClick
import io.data2viz.viz.KMouseMove
import io.data2viz.viz.TextHAlign
import io.data2viz.viz.TextNode
import io.data2viz.viz.TextVAlign
import io.data2viz.viz.Viz
import io.data2viz.viz.on
import io.data2viz.viz.viz
import penta.GameState
import penta.PentaColor
import penta.field.AbstractField
import penta.field.ConnectionField
import penta.figure.BlockerPiece
import penta.figure.Piece
import penta.figure.PlayerPiece

object PentaViz {
    private val pieces: MutableMap<String, Pair<CircleNode, TextNode?>> = mutableMapOf()
    val elements = mutableMapOf<AbstractField, Triple<CircleNode, TextNode, TextNode?>>()
    private var scale: Double = 100.0
    private var mousePos: Point = Point(0.0, 0.0)

    fun highlightedPieceAt(mousePos: Point): Piece? = gameState.findPieceAtPos(mousePos)?.let {
        // allow highlighting blockers when a piece is selected
        if(it !is PlayerPiece && gameState.selectedPlayerPiece == null) return@let null
        // TODO: remove highlighting pices when placing a blocker
        if(gameState.figurePositions[it.id] == null) return@let null
        it
    }

    val viz = viz {
        println("height: $height")
        println("width: $width")
        val scaleHCL = ScalesChromatic.Continuous.linearHCL {
            domain = PentaColor.values().map { it.ordinal * 72.0 * 3 }
            range = PentaColor.values().map { it.color }
        }
//    val outerCircle = circle {
//        stroke = scaleHCL
//        strokeWidth = 1.0
//        this.stroke =  0.col
//    }
        val outerCircle = (0..360 * 3).map {
            line {
                stroke = scaleHCL(360.0 * 3 - it.toDouble())
                strokeWidth = 3.0
            }
        }
        PentaBoard.fields.forEach { field ->
            println("adding: $field")
            val c = circle {
                strokeWidth = 2.0
                stroke = 0.col
                fill = field.color
            }
            val t1 = text {
                fontSize -= 2
                hAlign = TextHAlign.MIDDLE
                vAlign = TextVAlign.BASELINE
                this.textContent = field.id
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

            gameState.findPieceAtPos(mousePos)
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
            outerCircle.forEachIndexed { index, lineNode ->
                val angle = index.deg / 3

                with(lineNode) {
                    x1 = (PentaMath.r / PentaMath.R_ * scale / 2 - halfCircleWidth) * angle.cos + scale / 2
                    x2 = (PentaMath.r / PentaMath.R_ * scale / 2 + halfCircleWidth) * angle.cos + scale / 2
                    y1 = (PentaMath.r / PentaMath.R_ * scale / 2 - halfCircleWidth) * angle.sin + scale / 2
                    y2 = (PentaMath.r / PentaMath.R_ * scale / 2 + halfCircleWidth) * angle.sin + scale / 2
                }

            }
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
                    radius = field.radius / PentaMath.R_ * scale
//                    fill = if (field != highlightedField)
//                        field.color
//                    else
//                        field.color.brighten(2.0)
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

            gameState.figures.forEach {
                updatePiece(it)
            }
//            pieces.keys.forEach {
//                updatePiece(it)
//            }
        }
    }

    var gameState = GameState(
        listOf(),
        mapOf()
    )
        set(gameState) {
            gameState.updatePiece = ::updatePiece

            viz.apply {
                // clear old pieces
                pieces.values.forEach { (circle, text) ->
                    circle.remove()
                    text?.remove()
                }
                pieces.clear()

                // init pieces
                gameState.figures.forEach { piece ->
                    println("initialzing piece: $piece")
                    val c = circle {
                        strokeWidth = 1.0
                        stroke = 0.col
                    }
                    val t =
                        if (piece is PlayerPiece) {
                            text {
                                textContent = piece.playerId
                            }
                        } else null

                    pieces[piece.id] = c to t

                    updatePiece(piece)
                }
            }
            field = gameState
        }

    fun recolor() {
        val highlightedPiece = highlightedPieceAt(mousePos)
        val highlightedField = if (highlightedPiece == null) PentaBoard.findFieldAtPos(mousePos) else null
        elements.forEach { (field, triple) ->
            val (circle, text1, text2) = triple
            with(circle) {
                fill = if (field != highlightedField)
                    field.color
                else
                    field.color.brighten(2.0)
            }
        }
        gameState.figures.forEach {
            updatePiece(it)
        }
    }

    fun updatePiece(piece: Piece) {
        val highlightedPiece = highlightedPieceAt(mousePos)

        val (circle, text) = pieces[piece.id] ?: throw IllegalArgumentException("piece; $piece is not on the board")

//        val radius = when (piece) {
//            is BlockerPiece -> PentaMath.s / 2.5
//            is PlayerPiece -> PentaMath.s / 1.5
//            else -> throw NotImplementedError("unhandled piece type: ${piece::class}")
//        }
        val pos = piece.pos
        with(circle) {
            x = ((pos.x / PentaMath.R_)) * scale
            y = ((pos.y / PentaMath.R_)) * scale
            radius = piece.radius / PentaMath.R_ * scale
            fill = when (piece) {
                is PlayerPiece -> piece.color.color.brighten(1.0)
                is BlockerPiece -> piece.blockerType.color
                else -> throw IllegalStateException("unknown type ${piece::class}")
            }.let {
                when (piece) {
                    gameState.selectedPlayerPiece -> it.brighten(3.0)
                    highlightedPiece -> it.brighten(2.0)
                    else -> it
                }
            }
        }
        text?.apply {
            x = ((pos.x / PentaMath.R_)) * scale
            y = ((pos.y / PentaMath.R_)) * scale
            vAlign = TextVAlign.MIDDLE
            hAlign = TextHAlign.MIDDLE
        }
    }

    var hoveredField: AbstractField? = null
    var hoveredPiece: Piece? = null

    fun Viz.addEvents() {
        on(KMouseMove) { evt ->
            // println("Mouse Move:: ${evt.pos}")

            // convert pos back
//            mousePos = ((evt.pos / scale)- Point(0.5, 0.5)) * PentaMath.R_ * 2
            mousePos = (evt.pos / scale) * PentaMath.R_

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

            gameState.findPieceAtPos(mousePos)
                ?.also {
                    if (hoveredPiece != it) {
                        println("hover: $it")

                        hoveredPiece = it
                        hoveredField = null

                        // TODO: refactor - instead of resize create `recolor()`
//                        viz.resize(viz.width, viz.height)
                        recolor()
                        viz.render()
                    }
                }
                ?: PentaBoard.findFieldAtPos(mousePos)?.also {
                    if (hoveredField != it) {
                        println("hover: $it")

                        hoveredField = it
                        hoveredPiece = null

                        // TODO: refactor - instead of resize create `recolor()`
//                        viz.resize(viz.width, viz.height)
                        recolor()
                        viz.render()
                    }
                }
                ?: run {
                    //                    println("Mouse Move:: ${mousePos}")
                    if (hoveredField != null || hoveredPiece != null) {
                        hoveredField = null
                        hoveredPiece = null
//                        viz.resize(viz.width, viz.height)
                        recolor()
                        viz.render()
                    }
                }

        }
        on(KMouseClick) { evt ->
            //            println("MouseClick:: $evt")
//            println("shiftKey:: ${evt.shiftKey}")
//            println("ctrlKey:: ${evt.ctrlKey}")
//            println("metaKey:: ${evt.metaKey}")
//            println("ctrlKey:: ${evt.ctrlKey}")
            mousePos = (evt.pos / scale) * PentaMath.R_

            val piece = gameState.findPieceAtPos(mousePos)
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