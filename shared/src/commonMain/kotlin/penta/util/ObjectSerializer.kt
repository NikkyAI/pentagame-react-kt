package penta.util

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.internal.SerialClassDescImpl

@UseExperimental(kotlinx.serialization.InternalSerializationApi::class)
class ObjectSerializer<T : Any>(val obj: T) : KSerializer<T> {
    override val descriptor: SerialDescriptor = SerialClassDescImpl(obj::class.simpleName!!)
    override fun deserialize(decoder: Decoder): T {
        return obj
    }

    override fun serialize(encoder: Encoder, obj: T) {
        val composite = encoder.beginStructure(descriptor)
        composite.endStructure(descriptor)
    }
}