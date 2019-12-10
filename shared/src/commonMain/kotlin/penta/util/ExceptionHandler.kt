package penta.util

import kotlinx.coroutines.CoroutineExceptionHandler
import penta.IllegalMoveException
import penta.PentaMove
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

val handler = CoroutineExceptionHandler { _, exception ->
    println("Caught $exception")
}

@UseExperimental(ExperimentalContracts::class)
fun requireMove(value: Boolean, error: () -> PentaMove.IllegalMove): Nothing? {
    contract {
        returns() implies value
    }
    if (!value) {
        val illegalMove = error()
        throw IllegalMoveException(illegalMove)
    }
    return null
}
