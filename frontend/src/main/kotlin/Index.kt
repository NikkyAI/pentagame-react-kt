import components.app
import react.dom.render
import react.redux.provider
import reducers.State
import redux.RAction
import redux.compose
import redux.createStore
import redux.rEnhancer
import kotlin.browser.document

val store = createStore<State, RAction, dynamic>(
    State.combinedReducers(),
//    ::boardState,
//    BoardState.create(),
    State(),
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
