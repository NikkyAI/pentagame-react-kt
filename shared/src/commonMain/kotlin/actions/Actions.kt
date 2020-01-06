package actions

interface ActionHolder <A: Any> {
    val action: A
}

expect class Action<A: Any>(action: A): ActionHolder<A>