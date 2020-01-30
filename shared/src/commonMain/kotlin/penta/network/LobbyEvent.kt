package penta.network

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModuleBuilder
import penta.BoardState

@Polymorphic
@Serializable(PolymorphicSerializer::class)
sealed class LobbyEvent {
    interface FromClient
    interface FromServer


    // TODO: add more message types to update and remove games
    @Serializable
    data class UpdateGame(
        val game: GameSessionInfo
    ): LobbyEvent(), FromServer

    @Serializable
    data class Message(
        val userId: String,
        val content: String
    ): LobbyEvent(), FromClient, FromServer

    @Serializable
    data class Join(
        val userId: String
    ): LobbyEvent(), FromServer

    @Serializable
    data class Leave(
        val userId: String,
        val reason: String
    ): LobbyEvent(), FromServer

    @Serializable
    data class InitialSync(
        val users: List<String>,
        val chat: List<Message>,
        val games: List<GameSessionInfo>
    ): LobbyEvent(), FromServer

    // TODO: event to announce new games
    // TODO: event to invite players to games

    companion object {
        fun install(builder: SerializersModuleBuilder) {
            builder.polymorphic<LobbyEvent> {
                UpdateGame::class with UpdateGame.serializer()
                Message::class with Message.serializer()
                Join::class with Join.serializer()
                Leave::class with Leave.serializer()
                InitialSync::class with InitialSync.serializer()
            }
        }
    }
}