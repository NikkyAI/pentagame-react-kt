import mu.KotlinLogging
import penta.BoardState

class TestState() : BoardState() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    init {
        updateLogPanel = { t ->
            logger.info { t }
        }
    }

}