import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.Serializable
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import penta.PlayerState
import penta.UserInfo

@Polymorphic
@Serializable(PolymorphicSerializer::class)
sealed class SessionEvent {
    @Serializable
    data class WrappedGameEvent(
        val event: penta.network.GameEvent
    ) : SessionEvent()

    @Serializable
    data class PlayerJoin(
        val player: PlayerState, // TODO: make enum
        val user: UserInfo
    ) : SessionEvent()

    @Serializable
    class PlayerLeave(
        val player: PlayerState, // TODO: make enum
        val user: UserInfo
    ) : SessionEvent()

    @Serializable
    data class IllegalMove(
        val message: String,
        val move: WrappedGameEvent
    ) : SessionEvent() {

    }

    @Serializable
    data class Undo(
        val moves: List<WrappedGameEvent>
    ) : SessionEvent() {

    }

    companion object {
        fun install(builder: SerializersModuleBuilder) {
            builder.polymorphic<SessionEvent> {
                WrappedGameEvent::class with WrappedGameEvent.serializer()
                PlayerJoin::class with PlayerJoin.serializer()
                PlayerLeave::class with PlayerLeave.serializer()
                IllegalMove::class with IllegalMove.serializer()
            }
        }
    }
}