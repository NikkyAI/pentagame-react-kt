package penta

import PentaMath
import PentaViz
import io.data2viz.geom.Point
import io.data2viz.math.Angle
import io.data2viz.math.deg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import penta.logic.Piece
import penta.logic.field.AbstractField
import penta.logic.field.CornerField
import penta.util.length

class SingleplayerClientGameState(localPlayerCount: Int = 0) : ClientGameState() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    init {

    }


}