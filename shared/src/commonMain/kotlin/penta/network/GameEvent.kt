package penta.network

import PentaBoard
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModuleBuilder
import penta.BoardState
import penta.PentaMove
import penta.PlayerIds
import penta.logic.Piece
import penta.logic.GameType

@Polymorphic
@Serializable(PolymorphicSerializer::class)
sealed class GameEvent {
    // TODO: move into abstract class SerializedMove
    abstract fun asMove(boardState: BoardState): PentaMove

    @Serializable
    data class MovePlayer(
        val player: PlayerIds,
        val piece: String,
        val from: String,
        val to: String
    ) : GameEvent() {
        override fun asMove(boardState: BoardState) =
            PentaMove.MovePlayer(
                playerPiece = boardState.figures.filterIsInstance<Piece.Player>()
                    .first { it.player == player && it.id == piece },
                from = PentaBoard.get(from)!!,
                to = PentaBoard.get(to)!!
            )
    }

    @Serializable
    data class ForcedMovePlayer(
        val player: PlayerIds,
        val piece: String,
        val from: String,
        val to: String
    ) : GameEvent() {
        override fun asMove(boardState: BoardState) =
            PentaMove.ForcedPlayerMove(
                playerPiece = boardState.figures.filterIsInstance<Piece.Player>()
                    .first { it.player == player && it.id == piece },
                from = PentaBoard.get(from)!!,
                to = PentaBoard.get(to)!!
            )
    }

    @Serializable
    data class SwapOwnPiece(
        val player: PlayerIds,
        val piece: String,
        val otherPiece: String,
        val from: String,
        val to: String
    ) : GameEvent() {
        override fun asMove(boardState: BoardState) =
            PentaMove.SwapOwnPiece(
                playerPiece = boardState.figures.filterIsInstance<Piece.Player>()
                    .first { it.player == player && it.id == piece },
                otherPlayerPiece = boardState.figures.filterIsInstance<Piece.Player>()
                    .first { it.player == player && it.id == otherPiece },
                from = PentaBoard[from]!!,
                to = PentaBoard[to]!!
            )
    }

    @Serializable
    data class SwapHostilePieces(
        val player: PlayerIds,
        val otherPlayer: PlayerIds,
        val piece: String,
        val otherPiece: String,
        val from: String,
        val to: String
    ) : GameEvent() {
        override fun asMove(boardState: BoardState): PentaMove =
            PentaMove.SwapHostilePieces(
                playerPiece = boardState.figures.filterIsInstance<Piece.Player>()
                    .first { it.player == player && it.id == piece },
                otherPlayerPiece = boardState.figures.filterIsInstance<Piece.Player>()
                    .first { it.player == otherPlayer && it.id == otherPiece },
                from = PentaBoard.get(from)!!,
                to = PentaBoard.get(to)!!
            )
    }

    @Serializable
    data class CooperativeSwap(
        val player: PlayerIds,
        val otherPlayer: PlayerIds,
        val piece: String,
        val otherPiece: String,
        val from: String,
        val to: String
    ) : GameEvent() {
        override fun asMove(boardState: BoardState): PentaMove {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    @Serializable
    data class SetGrey(
        val id: String,
        val from: String?,
        val to: String
    ) : GameEvent() {
        override fun asMove(boardState: BoardState) =
            PentaMove.SetGrey(
                piece = boardState.figures.filterIsInstance<Piece.GrayBlocker>().first { it.id == id },
                from = from?.let { PentaBoard.get(it) },
                to = PentaBoard.get(to)!!
            )
    }

    @Serializable
    data class SetBlack(
        val id: String,
        val from: String,
        val to: String
    ) : GameEvent() {
        override fun asMove(boardState: BoardState) =
            PentaMove.SetBlack(
                piece = boardState.figures.filterIsInstance<Piece.BlackBlocker>().first { it.id == id },
                from = PentaBoard.get(from)!!,
                to = PentaBoard.get(to)!!
            )
    }

    @Serializable
    data class SelectGrey(
        val from: String,
//        val before: String?,
        val id: String?
    ) : GameEvent() {
        override fun asMove(boardState: BoardState) =
            PentaMove.SelectGrey(
                from = PentaBoard.get(from)!!,
//                before = before?.let { boardState.figures.filterIsInstance<Piece.GrayBlocker>().first { it.id == before } },
                grayPiece = id?.let { boardState.figures.filterIsInstance<Piece.GrayBlocker>().first { p -> p.id == it } }
            )
    }

    @Serializable
    data class SelectPlayerPiece(
        val before: String?,
        val id: String?
    ) : GameEvent() {
        override fun asMove(boardState: BoardState) =
            PentaMove.SelectPlayerPiece(
                before = before?.let { boardState.figures.filterIsInstance<Piece.Player>().first { p -> p.id == it } },
                playerPiece = id?.let { boardState.figures.filterIsInstance<Piece.Player>().first { p -> p.id == it } }
            )
    }

//    @Deprecated("move logic to SessionEvent")
//    @Serializable
//    data class PlayerJoin(
//        val player: PlayerState
//    ) : GameEvent() {
//        override fun asMove(boardState: BoardState) =
//            PentaMove.PlayerJoin(
//                player = player
//            )
//    }

    // TODO: also initialize player count / gamemode
    @Serializable
    data class SetGameType(
        val gameType: GameType
    ) : GameEvent() {
        override fun asMove(boardState: BoardState) =
            PentaMove.SetGameType(
                gameType = gameType
            )
    }

    @Serializable
    object InitGame: GameEvent() {
        override fun asMove(boardState: BoardState) = PentaMove.InitGame
    }

    @Serializable
    data class Win(
        val players: List<String>
    ) : GameEvent() {
        override fun asMove(boardState: BoardState) = PentaMove.Win(
            players = players
        )
    }

    @Deprecated("move logic to SessionEvent")
    @Serializable
    data class IllegalMove(
        val message: String,
        val move: GameEvent
    ) : GameEvent() {
        override fun asMove(boardState: BoardState): PentaMove {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    // TODO: move to SessionEvents
    @Serializable
    data class Undo(
        val moves: List<GameEvent>
    ) : GameEvent() {
        override fun asMove(boardState: BoardState): PentaMove = PentaMove.Undo(
            moves = moves.map { it }
        )
    }

    companion object {
        fun install(builder: SerializersModuleBuilder) {
            builder.polymorphic<GameEvent> {
                MovePlayer::class with MovePlayer.serializer()
                ForcedMovePlayer::class with ForcedMovePlayer.serializer()
                SwapOwnPiece::class with SwapOwnPiece.serializer()
                SwapHostilePieces::class with SwapHostilePieces.serializer()
                CooperativeSwap::class with CooperativeSwap.serializer()
                SetBlack::class with SetBlack.serializer()
                SetGrey::class with SetGrey.serializer()
                SelectGrey::class with SelectGrey.serializer()
                SelectPlayerPiece::class with SelectPlayerPiece.serializer()
//                PlayerJoin::class with PlayerJoin.serializer()
                SetGameType::class with SetGameType.serializer()
                InitGame::class with InitGame.serializer()
                Win::class with Win.serializer()
                IllegalMove::class with IllegalMove.serializer()
                Undo::class with Undo.serializer()
            }
        }
    }
}
