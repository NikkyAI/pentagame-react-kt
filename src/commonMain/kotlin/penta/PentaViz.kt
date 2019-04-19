import io.data2viz.color.col
import io.data2viz.geom.Point
import io.data2viz.math.deg
import io.data2viz.scale.ScalesChromatic
import io.data2viz.viz.CircleNode
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
    private val pieces: MutableMap<Piece, CircleNode> = mutableMapOf()
    private var scale: Double = 100.0
    private var mousePos: Point = Point(0.0, 0.0)

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
        val elements = mutableListOf<Triple<AbstractField, CircleNode, TextNode>>()
        PentaBoard.fields.forEach { field ->
            println("adding: $field")
            val c = circle {
                strokeWidth = 2.0
                stroke = 0.col
                fill = field.color
            }
            val t = text {
                textContent = field.id + ((field as? ConnectionField)?.altId?.let { "\n" + it } ?: "")
                fontSize -= 2
                hAlign = TextHAlign.MIDDLE
                vAlign = TextVAlign.MIDDLE
            }
            elements += Triple(field, c, t)
        }

        onResize { newWidth, newHeight ->
            println("resize")
            println("height: $height > $newHeight")
            println("width: $width > $newWidth")
            scale = kotlin.math.min(newWidth, newHeight)
            println("scale: $scale")

            gameState.findPieceAtPos(mousePos)
                ?.also {
                    println("selected: $it")
                }
                ?: PentaBoard.findFieldAtPos(mousePos)?.also {
                    println("selected: $it")
                }
                ?: run {
                    println("Mouse Move:: $mousePos")
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
            val highlightedPiece = gameState.findPieceAtPos(mousePos)
            val highlightedField = if(highlightedPiece == null) PentaBoard.findFieldAtPos(mousePos) else null

            elements.forEach { (field, circle, text) ->
                with(circle) {
                    x = ((field.pos.x / PentaMath.R_)) * scale
                    y = ((field.pos.y / PentaMath.R_)) * scale
                    radius = field.radius / PentaMath.R_ * scale
                    fill = if (field != highlightedField)
                        field.color
                    else
                        field.color.brighten(2.0)
                }
                with(text) {
                    x = ((field.pos.x / PentaMath.R_)) * scale
                    y = ((field.pos.y / PentaMath.R_)) * scale
                }
            }

            pieces.keys.forEach {
                updatePiece(it, highlightedPiece)
            }
        }
    }

    var gameState = GameState(
        listOf(),
        mapOf()
    )
        set(value) {
            value.updatePiece = ::updatePiece

            viz.apply {
                // clear old pieces
                pieces.values.forEach { circle ->
                    circle.remove()
                }
                pieces.clear()

                // init pieces
                value.figurePositions.entries.forEach { (piece, field) ->
                    println("initialzing piece: $piece")
                    val c = circle {
                        strokeWidth = 1.0
                        stroke = 0.col
                    }
                    pieces[piece] = c

                    val highlightedPiece = gameState.findPieceAtPos(mousePos)
                    updatePiece(piece, highlightedPiece)
                }
            }
            field = value
        }

    fun updatePiece(piece: Piece, highlightedPiece: Piece?) {
        val circle = pieces[piece] ?: throw IllegalArgumentException("piece; $piece is not on the board")

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
                if(piece == highlightedPiece)
                    it.brighten(2.0)
                else
                    it
            }
        }
    }

    var selectedField: AbstractField? = null
    var selectedPiece: Piece? = null

    fun Viz.addEvents() {
        on(KMouseMove) { evt ->
            // println("Mouse Move:: ${evt.pos}")

            // convert pos back
//            mousePos = ((evt.pos / scale)- Point(0.5, 0.5)) * PentaMath.R_ * 2
            mousePos = (evt.pos / scale) * PentaMath.R_

            gameState.findPieceAtPos(mousePos)
                ?.also {
                    if (selectedPiece != it) {
                        println("selected: $it")

                        selectedPiece = it
                        selectedField = null

                        viz.resize(viz.width, viz.height)
                        viz.render()
                    }
                }
                ?: PentaBoard.findFieldAtPos(mousePos)?.also {
                    if (selectedField != it) {
                        println("selected: $it")

                        selectedField = it
                        selectedPiece = null

                        viz.resize(viz.width, viz.height)
                        viz.render()
                    }
                }
                ?: run {
                    println("Mouse Move:: ${mousePos}")
                    if (selectedField != null || selectedPiece != null) {
                        selectedField = null
                        selectedPiece = null
                        viz.resize(viz.width, viz.height)
                        viz.render()
                    }
                }

        }
    }
}