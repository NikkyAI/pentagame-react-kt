package penta

import com.lightningkite.reacktive.property.StandardObservableProperty
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class PlayerState(
    val id: String,
    @SerialName("figureId") private val _figureId: String
) {
    @Transient
    val figureIdProperty = StandardObservableProperty(_figureId)
    var figureId: String
        get() = figureIdProperty.value
        set(value) {
            figureIdProperty.value = value
        }

    override fun toString(): String = "PlayerState(id: $id, figureId: $figureId)"
}