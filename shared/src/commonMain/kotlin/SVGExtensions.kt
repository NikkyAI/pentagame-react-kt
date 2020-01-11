package svg

import com.github.nwillc.ksvg.elements.CIRCLE
import com.github.nwillc.ksvg.elements.SVG

var CIRCLE.x: Double?
    get() {
        return cx?.toDoubleOrNull()
    }
    set(value: Double?) {
        cx = value?.toInt()?.toString()
    }
var CIRCLE.y: Double?
    get() {
        return cy?.toDoubleOrNull()
    }
    set(value: Double?) {
        cy = value?.toInt().toString()
    }
var CIRCLE.r_: Double?
    get() {
        return r?.toDoubleOrNull()
    }
    set(value: Double?) {
        r = value?.toInt().toString()
    }
