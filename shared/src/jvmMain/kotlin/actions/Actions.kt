package actions

actual data class Action <A: Any> actual constructor(override val action: A): ActionHolder<A>