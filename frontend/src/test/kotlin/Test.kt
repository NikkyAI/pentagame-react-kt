import mu.KotlinLogging
import org.reduxkotlin.applyMiddleware
import org.reduxkotlin.createStore
import penta.PentaMove
import penta.PlayerState
import penta.redux_rewrite.BoardState
import kotlin.test.*

class WrapperTest {
    private val logger = KotlinLogging.logger {}
    @Test
    fun wrapperTest() {
        val boardStore: org.reduxkotlin.Store<BoardState> = createStore(
            BoardState.reducer,
            BoardState.create(
                // TODO: remove default users
                listOf(PlayerState("alice", "cross"), PlayerState("bob", "triangle")),
                BoardState.GameType.TWO
            ),
            applyMiddleware(/*loggingMiddleware(logger)*/)
        )
        logger.info { "initialized" }

        boardStore.dispatch(PentaMove.PlayerJoin(PlayerState("eve", "square")))

        val wrapper = WrapperStore<BoardState, PentaMove, BoardState>(boardStore)
        logger.info { "initialized" }
        wrapper.dispatch(PentaMove.InitGame)
    }
}
