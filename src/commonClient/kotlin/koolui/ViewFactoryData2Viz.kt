package koolui

import com.lightningkite.koolui.canvas.Canvas
import com.lightningkite.reacktive.property.ConstantObservableProperty
import com.lightningkite.reacktive.property.ObservableProperty
import io.data2viz.viz.Viz

interface ViewFactoryData2Viz<VIEW> {
    /**
     * A canvas you can draw on.
     */
    fun vizCanvas(
        draw: ConstantObservableProperty<Viz>, postSetup: (Viz)->Unit
    ): VIEW
}