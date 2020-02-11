package penta.server

import io.ktor.application.ApplicationCall
import io.ktor.request.header
import io.ktor.response.header
import kotlin.random.Random

object SessionController {
    const val KEY = "SESSION"
    private val sessions: MutableMap<String, UserSession> = mutableMapOf()

    fun get(call: ApplicationCall): UserSession? {
        return call.request.header(KEY)?.let {
            sessions[it]
        }
    }

    fun get(sessionId: String): UserSession? {
        return sessions[sessionId]
    }

    fun set(session: UserSession, call: ApplicationCall) {
        val sessionId = getUnusedSessionId()
        sessions[sessionId] = session
        call.response.header(KEY, sessionId)
    }

    private fun getUnusedSessionId(): String {
        while (true) {
            val sessionId: String = Random.nextInt().toString(16)
            if (!sessions.containsKey(sessionId)) return sessionId
        }
    }
}