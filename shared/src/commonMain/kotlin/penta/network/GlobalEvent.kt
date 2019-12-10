package penta.network

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModuleBuilder

@Polymorphic
@Serializable(PolymorphicSerializer::class)
sealed class GlobalEvent {
    interface FromClient
    interface FromServer

    @Serializable
    data class Message(
        val userId: String,
        val content: String
    ): GlobalEvent(), FromClient, FromServer

    @Serializable
    data class Join(
        val userId: String
    ): GlobalEvent(), FromServer

    @Serializable
    data class Leave(
        val userId: String,
        val reason: String
    ): GlobalEvent(), FromServer

    @Serializable
    data class InitialSync(
        val users: List<String>,
        val chat: List<Message>
    ): GlobalEvent(), FromServer

    // TODO: event to announce new games
    // TODO: event to invite players to games

    companion object {
        fun install(builder: SerializersModuleBuilder) {
            builder.polymorphic<GlobalEvent> {
                Message::class with Message.serializer()
                Join::class with Join.serializer()
                Leave::class with Leave.serializer()
                InitialSync::class with InitialSync.serializer()
            }
        }
    }
}