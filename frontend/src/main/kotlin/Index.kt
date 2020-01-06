import components.app
import mu.KotlinLogging
import react.dom.render
import react.redux.provider
import reducers.State
import redux.RAction
import redux.compose
import redux.rEnhancer
import kotlin.browser.document

private val logger = KotlinLogging.logger {}
val store = redux.createStore<State, RAction, dynamic>(
    State.combinedReducers(),
    State(),
    compose(
        rEnhancer(),
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
