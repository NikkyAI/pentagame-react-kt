package penta

import com.lightningkite.reacktive.property.StandardObservableProperty

class PlayerState(
    val id: String,
    figureId: String
) {
    val figureIdProperty = StandardObservableProperty(figureId)
    var figureId: String
        get() = figureIdProperty.value
        set(value) {
            figureIdProperty.value = value
        }

    override fun toString(): String = "PlayerState(id: $id, figureId: $figureId)"
}