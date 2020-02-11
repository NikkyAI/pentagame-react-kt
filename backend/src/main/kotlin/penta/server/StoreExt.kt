package penta.server

import kotlinx.coroutines.runBlocking
import org.reduxkotlin.Dispatcher
import org.reduxkotlin.GetState
import org.reduxkotlin.Reducer
import org.reduxkotlin.Store
import org.reduxkotlin.StoreSubscriber
import org.reduxkotlin.StoreSubscription
import kotlin.coroutines.CoroutineContext

fun <State> sameThreadEnforcementWrapper(
    store: Store<State>,
    context: CoroutineContext
): Store<State> {
    return object : Store<State> {
        override var dispatch: Dispatcher
            get() = { 
                runBlocking(context) {
                    store.dispatch(it)
                }
            }
            set(value) {
                store.dispatch = value
            }
        override val getState: GetState<State>
            get() = {
                runBlocking(context) {
                    store.getState()
                }
            }
            
        override val replaceReducer: (Reducer<State>) -> Unit
            get() = {
                runBlocking(context) {
                    store.replaceReducer(it)
                }
            }
        override val subscribe: (StoreSubscriber) -> StoreSubscription
            get() = {
                runBlocking(context) {
                    store.subscribe(it)
                }
            }
    }
}
