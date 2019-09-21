package penta.notation

import penta.logic.field.AbstractField
import penta.logic.figure.BlackBlockerPiece
import penta.logic.figure.GrayBlockerPiece
import penta.logic.figure.Piece
import penta.logic.figure.PlayerPiece

sealed class PentaNotation(val token: String) {
    abstract fun serialize(): String

    class InitGame(val players: List<String>): PentaNotation("init") {
        override fun serialize(): String =
            "$token ${players.joinToString(",", "[", "]")}"
    }

    abstract class PlayerMovement(token: String): PentaNotation(token) {
        abstract val piece: PlayerPiece
        abstract val origin: AbstractField
        abstract val target: AbstractField
        abstract var moveGray: MoveGray?
    }

    class SwapPlayerPiece(
        override val piece: PlayerPiece,
        val otherPiece: PlayerPiece,
        override val origin: AbstractField,
        override val target: AbstractField,
        override var moveGray: MoveGray? = null
    ): PlayerMovement("swapPlayer") {
        override fun serialize(): String {
            return "$token ${piece.playerId} ${piece.id} ${origin.id} <-> ${target.id} ${otherPiece.id} ${otherPiece.playerId} [gray ${moveGray?.serialize()}]"
        }
    }

    class MovePlayerPiece(
        override val piece: PlayerPiece,
        override val origin: AbstractField,
        override val target: AbstractField,
        var moveBlack: MoveBlack? = null,
        override var moveGray: MoveGray? = null
    ): PlayerMovement("movePlayer") {
        override fun serialize(): String {
            return "$token ${piece.playerId} ${piece.id} ${origin.id} -> ${target.id} [black ${moveBlack?.serialize()}] [gray ${moveGray?.serialize()}]"
        }
    }

    class MoveBlack(
        val piece: BlackBlockerPiece,
        val origin: AbstractField,
        val target: AbstractField
    ): PentaNotation("moveBlack") {
        override fun serialize(): String {
            return "$token ${piece.id} ${origin.id} -> ${target.id}"
        }
    }

    class MoveGray(
        val piece: GrayBlockerPiece,
        val origin: AbstractField?,
        val target: AbstractField?
    ): PentaNotation("moveGray") {
        override fun serialize(): String {
            return "$token ${piece.id} ${origin?.id} -> ${target?.id}"
        }
    }

    class Win(
        val playerId: String
    ): PentaNotation("win") {
        override fun serialize(): String {
            return "$token $playerId"
        }
    }

    companion object {

        fun deserialize(notation: String): PentaNotation {
            TODO("implement the parser")
        }

    }

}