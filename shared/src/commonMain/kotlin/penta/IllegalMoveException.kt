package penta

data class IllegalMoveException(
    val move: PentaMove.IllegalMove
): IllegalStateException(move.message) {
}