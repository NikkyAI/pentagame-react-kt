package penta

import penta.logic.Field
import penta.logic.GameType
import penta.logic.Piece
import penta.network.GameEvent

sealed class PentaMove {
    abstract fun toSerializable(): GameEvent
    abstract fun asNotation(): String // TODO: move notation serializer/parser to GameEvent

    interface Move {
        val playerPiece: Piece.Player
        val from: Field
        val to: Field
    }

    interface Swap : Move {
        val otherPlayerPiece: Piece.Player
    }

    interface CanSetBlack
    interface CanSetGrey

    // ->
    data class MovePlayer(
        override val playerPiece: Piece.Player,
        override val from: Field,
        override val to: Field
    ) : PentaMove(), Move, CanSetBlack, CanSetGrey {
        override fun asNotation(): String = "${playerPiece.player}: ${playerPiece.id} (${from.id} -> ${to.id})"
        override fun toSerializable() =
            GameEvent.MovePlayer(
                player = playerPiece.player,
                piece = playerPiece.id,
                from = from.id,
                to = to.id
            )
    }

    // ->
    data class ForcedPlayerMove(
        override val playerPiece: Piece.Player,
        override val from: Field,
        override val to: Field
    ) : PentaMove(), Move, CanSetGrey {
        override fun asNotation(): String = "${playerPiece.player}: ${playerPiece.id} (${from.id} -> ${to.id})"
        override fun toSerializable() =
            GameEvent.ForcedMovePlayer(
                player = playerPiece.player,
                piece = playerPiece.id,
                from = from.id,
                to = to.id
            )
    }

    // <->
    data class SwapOwnPiece(
        override val playerPiece: Piece.Player,
        override val otherPlayerPiece: Piece.Player,
        override val from: Field,
        override val to: Field
    ) : PentaMove(), Swap, CanSetGrey {
        override fun asNotation(): String =
            "${playerPiece.player}: ${playerPiece.id} ${otherPlayerPiece.id}{${otherPlayerPiece.player}} (${from.id} <-> ${to.id})"

        override fun toSerializable() =
            GameEvent.SwapOwnPiece(
                player = playerPiece.player,
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
        override val from: Field,
        override val to: Field
    ) : PentaMove(), Swap, CanSetGrey {
        override fun asNotation(): String =
            "${playerPiece.player}: ${playerPiece.id} ${otherPlayerPiece.id}{${otherPlayerPiece.player}} (${from.id} <+> ${to.id})"

        override fun toSerializable() =
            GameEvent.SwapHostilePieces(
                player = playerPiece.player,
                otherPlayer = otherPlayerPiece.player,
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
        override val from: Field,
        override val to: Field
    ) : PentaMove(), Swap, CanSetGrey {
        override fun asNotation(): String =
            "${playerPiece.player}: ${playerPiece.id} ${otherPlayerPiece.id}{${otherPlayerPiece.player}} (${from.id} <=> ${to.id})"

        override fun toSerializable() =
            GameEvent.CooperativeSwap(
                player = playerPiece.player,
                otherPlayer = otherPlayerPiece.player,
                piece = playerPiece.id,
                otherPiece = otherPlayerPiece.id,
                from = from.id,
                to = to.id
            )
    }

    data class SetBlack(
        val piece: Piece.BlackBlocker,
        val from: Field,
        val to: Field
    ) : PentaMove() {
        override fun asNotation(): String = "& [${to.id}]"
        override fun toSerializable() = GameEvent.SetBlack(piece.id, from.id, to.id)
    }

    data class SetGrey(
        val piece: Piece.GrayBlocker,
        val from: Field?,
        val to: Field
    ) : PentaMove() {
        override fun asNotation(): String = "& [${to.id}]"
        override fun toSerializable() = GameEvent.SetGrey(piece.id, from?.id, to.id)
    }

    data class SelectGrey(
        val from: Field,
//        val before: Piece.GrayBlocker?,
        val grayPiece: Piece.GrayBlocker?
    ) : PentaMove() {
        override fun asNotation(): String = "select grey ${grayPiece?.id}"
        override fun toSerializable(): GameEvent = GameEvent.SelectGrey(
            from = from.id,
//            before = before?.id,
            id = grayPiece?.id
        )
    }

    data class SelectPlayerPiece(
        val before: Piece.Player?,
        val playerPiece: Piece.Player?
    ) : PentaMove() {
        override fun asNotation(): String = "select player ${playerPiece?.id}"
        override fun toSerializable(): GameEvent = GameEvent.SelectPlayerPiece(before?.id, playerPiece?.id)
    }

//    data class PlayerJoin(val player: PlayerState) : PentaMove() {
//        override fun asNotation(): String = ">>> [${player.id}]"
//        override fun toSerializable() = GameEvent.PlayerJoin(player)
//    }

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

    data class SetGameType(
        val gameType: GameType
    ): PentaMove() {
        override fun asNotation(): String = "chgametpe $gameType"
        override fun toSerializable() = GameEvent.SetGameType(
            gameType = gameType
        )
    }

    // TODO: also initialize player count / gamemode
    object InitGame: PentaMove() {
        override fun asNotation(): String = ">>> "
        override fun toSerializable() = GameEvent.InitGame
    }

    // is this a move ?
    data class Win(val players: List<String>) : PentaMove() {
        override fun asNotation(): String = "winner: ${players.joinToString(" & ")}"
        override fun toSerializable() = GameEvent.Win(players)
    }

    // TODO: session specific
    data class IllegalMove(val message: String, val move: PentaMove) : PentaMove() {
        override fun asNotation(): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun toSerializable() = GameEvent.IllegalMove(message, move.toSerializable())
    }

    data class Undo(val moves: List<GameEvent>) : PentaMove() {
        override fun asNotation(): String = "UNDO ${moves.map { it }}"
        override fun toSerializable(): GameEvent = GameEvent.Undo(
            moves.map { it }
        )
    }
}
