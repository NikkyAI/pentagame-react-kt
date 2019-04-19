package penta

import penta.field.AbstractField
import penta.figure.Piece

// TDO: add MoveGray?, MoveBlack?
data class Move(
    val player: String,
    val piece: Piece,
    val origin: AbstractField,
    val target: AbstractField
)

// TODO: serialization