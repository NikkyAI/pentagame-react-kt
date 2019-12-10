package koolui

import com.lightningkite.reacktive.property.ObservableProperty
import io.data2viz.viz.Viz

interface ViewFactoryData2Viz<VIEW> {
    /**
     * A canvas you can draw on.
     */
    fun vizCanvas(
        draw: ObservableProperty<Viz>
    ): VIEW
}