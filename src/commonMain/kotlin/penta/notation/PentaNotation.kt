package penta.notation

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule

@Polymorphic
@Serializable(PolymorphicSerializer::class)
sealed class PentaNotation() {
    abstract fun serialize(): String

    val token: String?
        get() = this::class.simpleName

    @Serializable
    class InitGame(val players: List<String>): PentaNotation() {
        override fun serialize(): String =
            "$token ${players.joinToString(",", "[", "]")}"
    }

    interface PlayerMovement {
        val playerPiece: String
        val playerId: String
        val origin: String
        val target: String
        var moveGray: MoveGray?
    }

    @Serializable
    class SwapPlayerPiece(
        override val playerPiece: String,
        override val playerId: String,
        val otherPiece: String,
        val otherPlayerId: String,
        override val origin: String,
        override val target: String,
        override var moveGray: MoveGray? = null
    ): PentaNotation(), PlayerMovement {
        override fun serialize(): String {
            return "$token ${playerId} ${playerPiece} ${origin} <-> ${target} ${otherPiece} ${otherPlayerId} [gray ${moveGray?.serialize()}]"
        }
    }

    @Serializable
    class MovePlayerPiece(
        override val playerPiece: String,
        override val playerId: String,
        override val origin: String,
        override val target: String,
        var moveBlack: MoveBlack? = null,
        override var moveGray: MoveGray? = null
    ): PentaNotation(), PlayerMovement {
        override fun serialize(): String {
            return "$token ${playerId} ${playerPiece} ${origin} -> ${target} [black ${moveBlack?.serialize()}] [gray ${moveGray?.serialize()}]"
        }
    }

    @Serializable
    class MoveBlack(
        val blackBlockerPiece: String,
        val origin: String,
        val target: String
    ): PentaNotation() {
        override fun serialize(): String {
            return "$token ${blackBlockerPiece} ${origin} -> ${target}"
        }
    }

    @Serializable
    class MoveGray(
        val grayBlockerPiece: String,
        val origin: String?,
        val target: String?
    ): PentaNotation() {
        override fun serialize(): String {
            return "$token ${grayBlockerPiece} ${origin} -> ${target}"
        }
    }

    @Serializable
    class Win(
        val playerId: String
    ): PentaNotation() {
        override fun serialize(): String {
            return "$token $playerId"
        }
    }

    companion object {
        val context = SerializersModule {
            polymorphic<PentaNotation> {
                InitGame::class with InitGame.serializer()
                SwapPlayerPiece::class with SwapPlayerPiece.serializer()
                MovePlayerPiece::class with MovePlayerPiece.serializer()
                MoveBlack::class with MoveBlack.serializer()
                MoveGray::class with MoveGray.serializer()
                Win::class with Win.serializer()
            }
        }
        fun deserialize(notation: String): PentaNotation {
            TODO("implement the parser")
        }

    }

}