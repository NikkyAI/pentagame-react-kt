import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.Serializable
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import penta.PlayerState
import penta.UserInfo
import penta.network.GameEvent

@Polymorphic
@Serializable(PolymorphicSerializer::class)
sealed class SessionEvent {
    @Serializable
    data class PlayerJoin(
        val player: PlayerState, // TODO: make enum
        val user: UserInfo // UserInfo
    ) : SessionEvent() {
//        override fun asMove(boardState: BoardState) =
//            PentaMove.PlayerJoin(
//                player = player
//            )
    }

    @Serializable
    data class IllegalMove(
        val message: String,
        val move: GameEvent
    ) : SessionEvent() {

    }

    @Serializable
    data class Undo(
        val moves: List<GameEvent>
    ) : SessionEvent() {

    }

    companion object {
        fun install(builder: SerializersModuleBuilder) {
            builder.polymorphic<SessionEvent> {
                PlayerJoin::class with PlayerJoin.serializer()
                IllegalMove::class with IllegalMove.serializer()
            }
        }
    }
}