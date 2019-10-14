package penta

import PentaBoard
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModuleBuilder
import mu.KotlinLogging
import penta.logic.Piece

@Polymorphic
@Serializable(PolymorphicSerializer::class)
sealed class SerialNotation {
    @Serializable
    data class MovePlayer(
        val player: String,
        val piece: String,
        val from: String,
        val to: String
    ) : SerialNotation()

    @Serializable
    data class ForcedMovePlayer(
        val player: String,
        val piece: String,
        val from: String,
        val to: String
    ) : SerialNotation()

    @Serializable
    data class SwapOwnPiece(
        val player: String,
        val piece: String,
        val otherPiece: String,
        val from: String,
        val to: String
    ) : SerialNotation()

    @Serializable
    data class SwapHostilePieces(
        val player: String,
        val otherPlayer: String,
        val piece: String,
        val otherPiece: String,
        val from: String,
        val to: String
    ) : SerialNotation()

    @Serializable
    data class CooperativeSwap(
        val player: String,
        val otherPlayer: String,
        val piece: String,
        val otherPiece: String,
        val from: String,
        val to: String
    ) : SerialNotation()

    @Serializable
    data class SetGrey(
        val id: String,
        val from: String?,
        val to: String
    ) : SerialNotation()

    @Serializable
    data class SetBlack(
        val id: String,
        val from: String,
        val to: String
    ) : SerialNotation()

    @Serializable
    data class InitGame(
        val players: List<PlayerState>
    ) : SerialNotation()

    @Serializable
    data class Win(
        val players: List<String>
    ) : SerialNotation()

    @Serializable
    data class IllegalMove(
        val message: String,
        val move: SerialNotation
    ): SerialNotation()

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
                InitGame::class with InitGame.serializer()
                Win::class with Win.serializer()
                IllegalMove::class with IllegalMove.serializer()
            }
        }

        fun toMoves(list: List<SerialNotation>, boardState: BoardState, combine: Boolean = true, action: (PentaMove) -> Unit): List<PentaMove> {
            val moves = mutableListOf<PentaMove>()
            val iter = list.iterator()

            while(iter.hasNext()) {
                val notation = iter.next()
                logger.debug{"converting: $notation"}

                fun getSetBlack(notation: SetBlack): PentaMove.SetBlack =
                    PentaMove.SetBlack(
                        piece =  boardState.figures.filterIsInstance<Piece.BlackBlocker>().first { it.id == notation.id },
                        from = PentaBoard.get(notation.from)!!,
                        to = PentaBoard.get(notation.to)!!
                    )
                fun getSetGrey(notation: SetGrey): PentaMove.SetGrey =
                    PentaMove.SetGrey(
                        piece =  boardState.figures.filterIsInstance<Piece.GrayBlocker>().first { it.id == notation.id },
                        from = notation.from?.let { PentaBoard.get(it) },
                        to = PentaBoard.get(notation.to)!!
                    )

                val move: PentaMove = when(notation) {
                    is MovePlayer -> PentaMove.MovePlayer(
                        playerPiece = boardState.figures.filterIsInstance<Piece.Player>().first { it.playerId == notation.player && it.id == notation.piece },
                        from = PentaBoard.get(notation.from)!!,
                        to =  PentaBoard.get(notation.to)!!
                    )
                    is ForcedMovePlayer -> PentaMove.ForcedPlayerMove(
                        playerPiece = boardState.figures.filterIsInstance<Piece.Player>().first { it.playerId == notation.player && it.id == notation.piece },
                        from = PentaBoard.get(notation.from)!!,
                        to =  PentaBoard.get(notation.to)!!
                    )
                    is SwapOwnPiece -> {
                        val playerpieces = boardState.figures.filterIsInstance<Piece.Player>()
                        logger.debug { playerpieces }
                        PentaMove.SwapOwnPiece(
                            playerPiece = boardState.figures.filterIsInstance<Piece.Player>().first { it.playerId == notation.player && it.id == notation.piece },
                            otherPlayerPiece = boardState.figures.filterIsInstance<Piece.Player>().first { it.playerId == notation.player && it.id == notation.otherPiece },
                            from = PentaBoard[notation.from]!!,
                            to =  PentaBoard[notation.to]!!
                        )
                    }
                    is SwapHostilePieces ->  PentaMove.SwapHostilePieces(
                        playerPiece = boardState.figures.filterIsInstance<Piece.Player>().first { it.playerId == notation.player && it.id == notation.piece },
                        otherPlayerPiece = boardState.figures.filterIsInstance<Piece.Player>().first { it.playerId == notation.otherPlayer && it.id == notation.otherPiece },
                        from = PentaBoard.get(notation.from)!!,
                        to =  PentaBoard.get(notation.to)!!
                    )
                    is CooperativeSwap -> TODO()
                    is SetGrey -> getSetGrey(notation)
                    is SetBlack -> getSetBlack(notation)
                    is InitGame -> PentaMove.InitGame(
                        players = notation.players
                    )
                    is Win -> PentaMove.Win(
                        players = notation.players
                    )
                    is IllegalMove -> TODO()
                }
                moves += move
                action(move)
            }
            return moves.toList()
        }
    }
}
