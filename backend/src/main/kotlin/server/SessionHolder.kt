package server

import kotlin.coroutines.*

data class Session(
    val userId : String
)

data class SessionHolder(var session: Session) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> get() = SessionHolder

    companion object : CoroutineContext.Key<SessionHolder>
}

suspend fun currentSessionHolder(): SessionHolder = coroutineContext[SessionHolder]!!
suspend fun currentSession(): Session = coroutineContext[SessionHolder]!!.session