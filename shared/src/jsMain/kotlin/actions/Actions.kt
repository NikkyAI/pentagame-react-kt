package actions

import redux.RAction

actual class Action <A: Any> actual constructor(override val action: A): RAction, ActionHolder<A>