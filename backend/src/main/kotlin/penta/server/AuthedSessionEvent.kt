package penta.server

import SessionEvent

data class AuthedSessionEvent(
    val event: SessionEvent,
    val user: User
) {

}