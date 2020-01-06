import org.reduxkotlin.Store
import redux.Reducer
import redux.Store as RStore

@Deprecated("nice try")
class WrapperStore<S, A: Any/*, R: Any*/> (
    val backingStore: Store<S>
) : RStore<S, A, Any> {
    override fun dispatch(action: A): Any {
        return backingStore.dispatch(action)
    }

    override fun getState(): S = backingStore.state

    override fun replaceReducer(nextReducer: Reducer<S, A>) {
        backingStore.replaceReducer(nextReducer as (S, Any) -> S)
    }

    override fun subscribe(listener: () -> Unit): () -> Unit =backingStore.subscribe(listener)
}