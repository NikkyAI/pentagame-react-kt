package penta

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModuleBuilder

@Polymorphic
@Serializable(PolymorphicSerializer::class)
sealed class SerialNotation {
    @Serializable
    data class MovePlayer(
        val player: String,
        val piece: String,
        val from: String,
        val to: String,
        val setBlack: Boolean,
        val setGrey: Boolean
    ) : SerialNotation()

    @Serializable
    data class ForcedMovePlayer(
        val player: String,
        val piece: String,
        val from: String,
        val to: String,
        val setGrey: Boolean
    ) : SerialNotation()

    @Serializable
    data class SwapOwnPiece(
        val player: String,
        val piece: String,
        val otherPiece: String,
        val from: String,
        val to: String,
        val setGrey: Boolean
    ) : SerialNotation()

    @Serializable
    data class SwapHostilePieces(
        val player: String,
        val otherPlayer: String,
        val piece: String,
        val otherPiece: String,
        val from: String,
        val to: String,
        val setGrey: Boolean
    ) : SerialNotation()

    @Serializable
    data class CooperativeSwap(
        val player: String,
        val otherPlayer: String,
        val piece: String,
        val otherPiece: String,
        val from: String,
        val to: String,
        val setGrey: Boolean
    ) : SerialNotation()

    @Serializable
    data class SetGrey(
        val from: String?,
        val to: String
    ) : SerialNotation()

    @Serializable
    data class SetBlack(
        val from: String,
        val to: String
    ) : SerialNotation()

    @Serializable
    data class InitGame(
        val players: List<String>
    ) : SerialNotation()

    @Serializable
    data class Win(
        val players: List<String>
    ) : SerialNotation()

    @Serializable
    data class IllegalMove(
        val move: SerialNotation
    ): SerialNotation()

    companion object {
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
            }
        }
    }
}
