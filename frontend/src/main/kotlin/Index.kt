import components.app
import penta.redux_rewrite.BoardState
import react.dom.render
import react.redux.provider
import reducers.State.Companion.boardState
import redux.RAction
import redux.compose
import redux.createStore
import redux.rEnhancer
import kotlin.browser.document

//private val logger = KotlinLogging.logger {}

val store = createStore<BoardState, RAction, dynamic>(
//    State.combinedReducers(),
    ::boardState,
    BoardState.create(),
//    rEnhancer()
    compose(
        rEnhancer(),
//        applyMiddleware<State, RAction, State, dynamic, dynamic>(
//            createLogger(object: ReduxLoggerOptions {
//                override var logger: Any?
//                    get() = super.logger
//                    set(value) {}
//            })
//            { middleWareApi ->
//                { actionFun ->
////                    console.log("actionFun: $actionFun")
//                    { action ->
//                        console.log("action: $action")
//                        actionFun(action)
//                    }
//                }
//            }
//        ),
        js("if(window.__REDUX_DEVTOOLS_EXTENSION__ )window.__REDUX_DEVTOOLS_EXTENSION__ ();else(function(f){return f;});")
    )
)

fun main() {
//    logger.info { "store initialized" }
//    store.dispatch(
//        Action(
//            PentaMove.PlayerJoin(PlayerState("eve", "square"))
//        )
//    )
//    store.dispatch(
//        Action(
//            PentaMove.InitGame
//        )
//    )

    val rootDiv = document.getElementById("container")
    render(rootDiv) {
        provider(store) {
            app()
        }
    }
}
