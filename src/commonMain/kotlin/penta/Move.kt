package penta

import penta.field.AbstractField
import penta.figure.Piece
import kotlin.jvm.JvmOverloads

// TODO: add MoveGray?, MoveBlack?

data class Move @JvmOverloads constructor(
    val player: String,
    val piece: Piece,
    val origin: AbstractField,
    val target: AbstractField
)

// TODO: serialization