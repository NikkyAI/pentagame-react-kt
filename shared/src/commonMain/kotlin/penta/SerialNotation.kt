package penta

import PentaBoard
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModuleBuilder
import mu.KotlinLogging
import penta.logic.Piece
import penta.redux_rewrite.BoardState
import penta.util.ObjectSerializer

//TODO: refactor to penta.network.GameEvent

@Polymorphic
@Serializable(PolymorphicSerializer::class)
sealed class SerialNotation {
    // TODO: move into abstract class SerializedMove
    abstract fun asMove(boardState: BoardState): PentaMove

    @Serializable
    data class MovePlayer(
        val player: String,
        val piece: String,
        val from: String,
        val to: String
    ) : SerialNotation() {
        override fun asMove(boardState: BoardState) =
            PentaMove.MovePlayer(
                playerPiece = boardState.figures.filterIsInstance<Piece.Player>().first { it.playerId == player && it.id == piece },
                from = PentaBoard.get(from)!!,
                to = PentaBoard.get(to)!!
            )
    }

    @Serializable
    data class ForcedMovePlayer(
        val player: String,
        val piece: String,
        val from: String,
        val to: String
    ) : SerialNotation() {
        override fun asMove(boardState: BoardState) =
            PentaMove.ForcedPlayerMove(
                playerPiece = boardState.figures.filterIsInstance<Piece.Player>().first { it.playerId == player && it.id == piece },
                from = PentaBoard.get(from)!!,
                to = PentaBoard.get(to)!!
            )
    }

    @Serializable
    data class SwapOwnPiece(
        val player: String,
        val piece: String,
        val otherPiece: String,
        val from: String,
        val to: String
    ) : SerialNotation() {
        override fun asMove(boardState: BoardState) =
            PentaMove.SwapOwnPiece(
                playerPiece = boardState.figures.filterIsInstance<Piece.Player>().first { it.playerId == player && it.id == piece },
                otherPlayerPiece = boardState.figures.filterIsInstance<Piece.Player>().first { it.playerId == player && it.id == otherPiece },
                from = PentaBoard[from]!!,
                to = PentaBoard[to]!!
            )
    }

    @Serializable
    data class SwapHostilePieces(
        val player: String,
        val otherPlayer: String,
        val piece: String,
        val otherPiece: String,
        val from: String,
        val to: String
    ) : SerialNotation() {
        override fun asMove(boardState: BoardState): PentaMove =
            PentaMove.SwapHostilePieces(
                playerPiece = boardState.figures.filterIsInstance<Piece.Player>().first { it.playerId == player && it.id == piece },
                otherPlayerPiece = boardState.figures.filterIsInstance<Piece.Player>().first { it.playerId == otherPlayer && it.id == otherPiece },
                from = PentaBoard.get(from)!!,
                to = PentaBoard.get(to)!!
            )
    }

    @Serializable
    data class CooperativeSwap(
        val player: String,
        val otherPlayer: String,
        val piece: String,
        val otherPiece: String,
        val from: String,
        val to: String
    ) : SerialNotation() {
        override fun asMove(boardState: BoardState): PentaMove {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    @Serializable
    data class SetGrey(
        val id: String,
        val from: String?,
        val to: String
    ) : SerialNotation() {
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
    ) : SerialNotation() {
        override fun asMove(boardState: BoardState) =
            PentaMove.SetBlack(
                piece = boardState.figures.filterIsInstance<Piece.BlackBlocker>().first { it.id == id },
                from = PentaBoard.get(from)!!,
                to = PentaBoard.get(to)!!
            )
    }

    @Serializable
    data class PlayerJoin(
        val player: PlayerState
    ) : SerialNotation() {
        override fun asMove(boardState: BoardState) =
            PentaMove.PlayerJoin(
                player = player
            )
    }

    object InitGame : SerialNotation() {
        override fun asMove(boardState: BoardState) =
            PentaMove.InitGame
    }

    @Serializable
    data class Win(
        val players: List<String>
    ) : SerialNotation() {
        override fun asMove(boardState: BoardState) = PentaMove.Win(
            players = players
        )
    }

    @Serializable
    data class IllegalMove(
        val message: String,
        val move: SerialNotation
    ) : SerialNotation() {
        override fun asMove(boardState: BoardState): PentaMove {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
        fun install(builder: SerializersModuleBuilder) {
            builder.polymorphic<SerialNotation> {
                MovePlayer::class with MovePlayer.serializer()
                ForcedMovePlayer::class with ForcedMovePlayer.serializer()
                SwapOwnPiece::class with SwapOwnPiece.serializer()
                SwapHostilePieces::class with SwapHostilePieces.serializer()
                CooperativeSwap::class with CooperativeSwap.serializer()
                SetBlack::class with SetBlack.serializer()
                SetGrey::class with SetGrey.serializer()
                PlayerJoin::class with PlayerJoin.serializer()
                InitGame::class with ObjectSerializer(InitGame)
                Win::class with Win.serializer()
                IllegalMove::class with IllegalMove.serializer()
            }
        }
    }
}
