package penta

import penta.logic.field.AbstractField
import penta.logic.figure.Piece
import kotlin.jvm.JvmOverloads

// TODO: add MoveGray?, MoveBlack?

data class Move @JvmOverloads constructor(
    val player: String,
    val piece: Piece,
    val origin: AbstractField,
    val target: AbstractField
)

// TODO: serialization