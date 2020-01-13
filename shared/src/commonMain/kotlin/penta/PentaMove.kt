package penta

import penta.logic.Piece
import penta.logic.field.AbstractField

sealed class PentaMove {
    abstract fun toSerializable(): SerialNotation
    abstract fun asNotation(): String

    interface Move {
        val playerPiece: Piece.Player
        val from: AbstractField
        val to: AbstractField
    }

    interface Swap : Move {
        val otherPlayerPiece: Piece.Player
    }

    interface CanSetBlack
    interface CanSetGrey

    // ->
    data class MovePlayer(
        override val playerPiece: Piece.Player,
        override val from: AbstractField,
        override val to: AbstractField
    ) : PentaMove(), Move, CanSetBlack, CanSetGrey {
        override fun asNotation(): String = "${playerPiece.playerId}: ${playerPiece.id} (${from.id} -> ${to.id})"
        override fun toSerializable() =
            SerialNotation.MovePlayer(
                player = playerPiece.playerId,
                piece = playerPiece.id,
                from = from.id,
                to = to.id
            )
    }

    // ->
    data class ForcedPlayerMove(
        override val playerPiece: Piece.Player,
        override val from: AbstractField,
        override val to: AbstractField
    ) : PentaMove(), Move, CanSetGrey {
        override fun asNotation(): String = "${playerPiece.playerId}: ${playerPiece.id} (${from.id} -> ${to.id})"
        override fun toSerializable() =
            SerialNotation.ForcedMovePlayer(
                player = playerPiece.playerId,
                piece = playerPiece.id,
                from = from.id,
                to = to.id
            )
    }

    // <->
    data class SwapOwnPiece(
        override val playerPiece: Piece.Player,
        override val otherPlayerPiece: Piece.Player,
        override val from: AbstractField,
        override val to: AbstractField
    ) : PentaMove(), Swap, CanSetGrey {
        override fun asNotation(): String =
            "${playerPiece.playerId}: ${playerPiece.id} ${otherPlayerPiece.id}{${otherPlayerPiece.playerId}} (${from.id} <-> ${to.id})"

        override fun toSerializable() =
            SerialNotation.SwapOwnPiece(
                player = playerPiece.playerId,
                piece = playerPiece.id,
                otherPiece = otherPlayerPiece.id,
                from = from.id,
                to = to.id
            )
    }

    // <-/->
    data class SwapHostilePieces(
        override val playerPiece: Piece.Player,
        override val otherPlayerPiece: Piece.Player,
        override val from: AbstractField,
        override val to: AbstractField
    ) : PentaMove(), Swap, CanSetGrey {
        override fun asNotation(): String =
            "${playerPiece.playerId}: ${playerPiece.id} ${otherPlayerPiece.id}{${otherPlayerPiece.playerId}} (${from.id} <+> ${to.id})"

        override fun toSerializable() =
            SerialNotation.SwapHostilePieces(
                player = playerPiece.playerId,
                otherPlayer = otherPlayerPiece.playerId,
                piece = playerPiece.id,
                otherPiece = otherPlayerPiece.id,
                from = from.id,
                to = to.id
            )
    }

    // <=>
    data class CooperativeSwap(
        override val playerPiece: Piece.Player,
        override val otherPlayerPiece: Piece.Player,
        override val from: AbstractField,
        override val to: AbstractField
    ) : PentaMove(), Swap, CanSetGrey {
        override fun asNotation(): String =
            "${playerPiece.playerId}: ${playerPiece.id} ${otherPlayerPiece.id}{${otherPlayerPiece.playerId}} (${from.id} <=> ${to.id})"

        override fun toSerializable() =
            SerialNotation.CooperativeSwap(
                player = playerPiece.playerId,
                otherPlayer = otherPlayerPiece.playerId,
                piece = playerPiece.id,
                otherPiece = otherPlayerPiece.id,
                from = from.id,
                to = to.id
            )
    }

    data class SetBlack(
        val piece: Piece.BlackBlocker,
        val from: AbstractField,
        val to: AbstractField
    ) : PentaMove() {
        override fun asNotation(): String = "& [${to.id}]"
        override fun toSerializable() = SerialNotation.SetBlack(piece.id, from.id, to.id)
    }

    data class SetGrey(
        val piece: Piece.GrayBlocker,
        val from: AbstractField?,
        val to: AbstractField
    ) : PentaMove() {
        override fun asNotation(): String = "& [${to.id}]"
        override fun toSerializable() = SerialNotation.SetGrey(piece.id, from?.id, to.id)
    }

    data class SelectGrey(
        val grayPiece: Piece.GrayBlocker?
    ): PentaMove() {
        override fun asNotation(): String = "select grey ${grayPiece?.id}"
        override fun toSerializable(): SerialNotation = TODO("add serializer class")
    }

    data class SelectPlayerPiece(
        val playerPiece: Piece.Player?
    ): PentaMove() {
        override fun asNotation(): String = "select player ${playerPiece?.id}"
        override fun toSerializable(): SerialNotation = TODO("add serializer class")
    }

    data class PlayerJoin(val player: PlayerState) : PentaMove() {
        override fun asNotation(): String = ">>> [${player.id}]"
        override fun toSerializable() = SerialNotation.PlayerJoin(player)
    }

//    @Deprecated("not a move")
//    data class ObserverJoin(val id: String) : PentaMove() {
//        override fun asNotation(): String = "join [${id}]"
//        override fun toSerializable() = GameEvent.ObserverJoin(id)
//    }
//
//    @Deprecated("not a move")
//    data class ObserverLeave(val id: String) : PentaMove() {
//        override fun asNotation(): String = "leave [${id}]"
//        override fun toSerializable() = GameEvent.ObserverLeave(id)
//    }

    object InitGame : PentaMove() {
        override fun asNotation(): String = ">>>"
        override fun toSerializable() = SerialNotation.InitGame
        override fun toString() = "InitGame"
    }

    data class Win(val players: List<String>) : PentaMove() {
        override fun asNotation(): String = "winner: ${players.joinToString(" & ")}"
        override fun toSerializable() = SerialNotation.Win(players)
    }

    data class IllegalMove(val message: String, val move: PentaMove) : PentaMove() {
        override fun asNotation(): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun toSerializable() = SerialNotation.IllegalMove(message, move.toSerializable())
    }
}
