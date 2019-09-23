package penta

import penta.logic.field.AbstractField
import penta.logic.Piece

sealed class PentaMove {
    // TODO: force via abstract
    abstract fun toSerializableList(): List<SerialNotation>

    interface Move {
        val playerPiece: Piece.Player
        val from: AbstractField
        val to: AbstractField
    }

    interface Swap : Move {
        val otherPlayerPiece: Piece.Player
    }

    interface CanSetBlack {
        var setBlack: SetBlack?
    }

    interface CanSetGrey {
        var setGrey: SetGrey?
    }

    // ->
    data class MovePlayer(
        override val playerPiece: Piece.Player,
        override val from: AbstractField,
        override val to: AbstractField,
        override var setBlack: SetBlack? = null,
        override var setGrey: SetGrey? = null
    ) : PentaMove(), Move, CanSetBlack, CanSetGrey {
        override fun toSerializableList() =
            listOfNotNull(
                SerialNotation.MovePlayer(
                    player = playerPiece.playerId,
                    piece = playerPiece.id,
                    from = from.id,
                    to = to.id,
                    setBlack = setBlack != null,
                    setGrey = setGrey != null
                ),
                setGrey?.serialize(),
                setGrey?.serialize()
            )
    }

    // ->
    data class ForcedPlayerMove(
        override val playerPiece: Piece.Player,
        override val from: AbstractField,
        override val to: AbstractField,
        override var setGrey: SetGrey? = null
    ) : PentaMove(), Move, CanSetGrey {
        override fun toSerializableList() =
            listOfNotNull(
                SerialNotation.ForcedMovePlayer(
                    player = playerPiece.playerId,
                    piece = playerPiece.id,
                    from = from.id,
                    to = to.id,
                    setGrey = setGrey != null
                ),
                setGrey?.serialize(),
                setGrey?.serialize()
            )
    }

    // <->
    data class SwapOwnPiece(
        override val playerPiece: Piece.Player,
        override val otherPlayerPiece: Piece.Player,
        override val from: AbstractField,
        override val to: AbstractField,
        override var setGrey: SetGrey? = null
    ) : PentaMove(), Swap, CanSetGrey {
        override fun toSerializableList() =
            listOfNotNull(
                SerialNotation.SwapOwnPiece(
                    player = playerPiece.playerId,
                    piece = playerPiece.id,
                    otherPiece = otherPlayerPiece.id,
                    from = from.id,
                    to = to.id,
                    setGrey = setGrey != null
                ),
                setGrey?.toSerializableList()?.first()
            )
    }

    // <-/->
    data class SwapHostilePieces(
        override val playerPiece: Piece.Player,
        override val otherPlayerPiece: Piece.Player,
        override val from: AbstractField,
        override val to: AbstractField,
        override var setGrey: SetGrey? = null
    ) : PentaMove(), Swap, CanSetGrey {
        override fun toSerializableList() =
            listOfNotNull(
                SerialNotation.SwapHostilePieces(
                    player = playerPiece.playerId,
                    otherPlayer = otherPlayerPiece.playerId,
                    piece = playerPiece.id,
                    otherPiece = otherPlayerPiece.id,
                    from = from.id,
                    to = to.id,
                    setGrey = setGrey != null
                ),
                setGrey?.serialize()
            )
    }

    // <=>
    data class CooperativeSwap(
        override val playerPiece: Piece.Player,
        override val otherPlayerPiece: Piece.Player,
        override val from: AbstractField,
        override val to: AbstractField,
        override var setGrey: SetGrey? = null
    ) : PentaMove(), Swap, CanSetGrey {
        override fun toSerializableList() =
            listOfNotNull(
                SerialNotation.CooperativeSwap(
                    player = playerPiece.playerId,
                    otherPlayer = otherPlayerPiece.playerId,
                    piece = playerPiece.id,
                    otherPiece = otherPlayerPiece.id,
                    from = from.id,
                    to = to.id,
                    setGrey = setGrey != null
                ),
                setGrey?.toSerializableList()?.first()
            )
    }

    data class SetBlack(
        val piece: Piece.BlackBlocker,
        val from: AbstractField,
        val to: AbstractField
    ) : PentaMove() {
        fun toNotation() = SerialNotation.SetBlack(from.id, to.id)
        override fun toSerializableList() = listOf(
            toNotation()
        )
    }

    data class SetGrey(
        val piece: Piece.GrayBlocker,
        val from: AbstractField?,
        val to: AbstractField
    ) : PentaMove() {
        fun serialize() = SerialNotation.SetGrey(from?.id, to.id)
        override fun toSerializableList() = listOf(
            serialize()
        )
    }

    data class InitGame(val players: List<String>) : PentaMove() {
        fun serialize() = SerialNotation.InitGame(players)
        override fun toSerializableList() = listOf(
            serialize()
        )
    }

    data class Win(val player: String) : PentaMove() {
        fun serialize() = SerialNotation.Win(player)
        override fun toSerializableList() = listOf(
            serialize()
        )
    }
}
