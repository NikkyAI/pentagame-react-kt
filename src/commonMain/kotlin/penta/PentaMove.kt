package penta

import penta.logic.field.AbstractField
import penta.logic.Piece

sealed class PentaMove {
    abstract fun toSerializableList(): List<SerialNotation>
    abstract fun asNotation(): String

    interface Move {
        val playerPiece: Piece.Player
        val from: AbstractField
        val to: AbstractField
    }

    interface Swap : Move {
        val otherPlayerPiece: Piece.Player
    }

    interface CanSetBlack {
        val setBlack: SetBlack?
        fun withSetBlack(setBlack: SetBlack): PentaMove
    }

    interface CanSetGrey {
        val setGrey: SetGrey?
        fun withSetGrey(setGrey: SetGrey): PentaMove
    }

    // ->
    data class MovePlayer(
        override val playerPiece: Piece.Player,
        override val from: AbstractField,
        override val to: AbstractField,
        override val setBlack: SetBlack? = null,
        override val setGrey: SetGrey? = null
    ) : PentaMove(), Move, CanSetBlack, CanSetGrey {
        override fun asNotation(): String = "${playerPiece.playerId}: ${playerPiece.id} (${from.id} -> ${to.id})" +
            (setBlack?.asNotation() ?: "") +
            (setGrey?.asNotation() ?: "")
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
                setBlack?.serialize(),
                setGrey?.serialize()
            )

        override fun withSetBlack(setBlack: SetBlack) = copy(setBlack = setBlack)
        override fun withSetGrey(setGrey: SetGrey) = copy(setGrey = setGrey)
    }

    // ->
    data class ForcedPlayerMove(
        override val playerPiece: Piece.Player,
        override val from: AbstractField,
        override val to: AbstractField,
        override val setGrey: SetGrey? = null
    ) : PentaMove(), Move, CanSetGrey {
        override fun asNotation(): String = "${playerPiece.playerId}: ${playerPiece.id} (${from.id} -> ${to.id})" +
            (setGrey?.asNotation() ?: "")
        override fun toSerializableList() =
            listOfNotNull(
                SerialNotation.ForcedMovePlayer(
                    player = playerPiece.playerId,
                    piece = playerPiece.id,
                    from = from.id,
                    to = to.id,
                    setGrey = setGrey != null
                ),
                setGrey?.serialize()
            )
        override fun withSetGrey(setGrey: SetGrey) = copy(setGrey = setGrey)
    }

    // <->
    data class SwapOwnPiece(
        override val playerPiece: Piece.Player,
        override val otherPlayerPiece: Piece.Player,
        override val from: AbstractField,
        override val to: AbstractField,
        override val setGrey: SetGrey? = null
    ) : PentaMove(), Swap, CanSetGrey {
        override fun asNotation(): String = "${playerPiece.playerId}: ${playerPiece.id} ${otherPlayerPiece.id}{${otherPlayerPiece.playerId}} (${from.id} <-> ${to.id})"

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
        override fun withSetGrey(setGrey: SetGrey) = copy(setGrey = setGrey)
    }

    // <-/->
    data class SwapHostilePieces(
        override val playerPiece: Piece.Player,
        override val otherPlayerPiece: Piece.Player,
        override val from: AbstractField,
        override val to: AbstractField,
        override val setGrey: SetGrey? = null
    ) : PentaMove(), Swap, CanSetGrey {
        override fun asNotation(): String = "${playerPiece.playerId}: ${playerPiece.id} ${otherPlayerPiece.id}{${otherPlayerPiece.playerId}} (${from.id} <+> ${to.id})"

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
        override fun withSetGrey(setGrey: SetGrey) = copy(setGrey = setGrey)
    }

    // <=>
    data class CooperativeSwap(
        override val playerPiece: Piece.Player,
        override val otherPlayerPiece: Piece.Player,
        override val from: AbstractField,
        override val to: AbstractField,
        override val setGrey: SetGrey? = null
    ) : PentaMove(), Swap, CanSetGrey {
        override fun asNotation(): String = "${playerPiece.playerId}: ${playerPiece.id} ${otherPlayerPiece.id}{${otherPlayerPiece.playerId}} (${from.id} <=> ${to.id})"

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
        override fun withSetGrey(setGrey: SetGrey) = copy(setGrey = setGrey)
    }

    data class SetBlack(
        val piece: Piece.BlackBlocker,
        val from: AbstractField,
        val to: AbstractField
    ) : PentaMove() {
        override fun asNotation(): String = "& [${to.id}]"
        fun serialize() = SerialNotation.SetBlack(piece.id, from.id, to.id)
        override fun toSerializableList() = listOf(
            serialize()
        )
    }

    data class SetGrey(
        val piece: Piece.GrayBlocker,
        val from: AbstractField?,
        val to: AbstractField
    ) : PentaMove() {
        override fun asNotation(): String = "& [${to.id}]"
        fun serialize() = SerialNotation.SetGrey(piece.id, from?.id, to.id)
        override fun toSerializableList() = listOf(
            serialize()
        )
    }

    data class InitGame(val players: List<PlayerState>) : PentaMove() {
        override fun asNotation(): String = ">>> [${players.joinToString(" & ") { it.id }}]"
        fun serialize() = SerialNotation.InitGame(players)
        override fun toSerializableList() = listOf(
            serialize()
        )
    }

    data class Win(val players: List<String>) : PentaMove() {
        override fun asNotation(): String = "winner: ${players.joinToString(" & ")}"
        fun serialize() = SerialNotation.Win(players)
        override fun toSerializableList() = listOf(
            serialize()
        )
    }
}
