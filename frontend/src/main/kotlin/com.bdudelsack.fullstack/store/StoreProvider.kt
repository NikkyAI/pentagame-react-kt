package com.bdudelsack.fullstack.store

import react.RBuilder
import react.createContext
import react.useContext

data class StoreContext(
    val store: Store? = null
)

var storeContext = createContext(StoreContext())

fun RBuilder.storeProvider(store: Store, childrenBuilder: RBuilder.() -> Unit) {
    storeContext.Provider {
        attrs {
            value = StoreContext(store)
        }
        childrenBuilder()
    }
}

fun useStore(): Store {
    val context = useContext(storeContext)
    return context.store!!
}
