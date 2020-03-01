package penta

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PrimitiveDescriptor
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializable

// TODO: switch to enum ?
@Serializable(with = PlayerIds.Companion::class)
enum class PlayerIds {
    PLAYER_1, PLAYER_2, PLAYER_3, PLAYER_4;

    val id: String
        get() = this.name

    companion object : KSerializer<PlayerIds> {
        override val descriptor: SerialDescriptor
            get() = PrimitiveDescriptor("PlayerIds", PrimitiveKind.STRING)    // SerialDescriptor("PlayerIds", kind = StructureKind.OBJECT)

        override fun deserialize(decoder: Decoder): PlayerIds {
            return valueOf(decoder.decodeString())
        }

        override fun serialize(encoder: Encoder, value: PlayerIds) {
            encoder.encodeString(value.name)
        }
    }
}