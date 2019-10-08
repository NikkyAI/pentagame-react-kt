import penta.BoardState

class TestState() : BoardState() {
    init {
        updateLogPanel = { t ->
            println(t)
        }
    }

}