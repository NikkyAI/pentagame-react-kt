package old.server

import penta.UserInfo

data class ServerUserInfo(
    val user: User,
    var figureId: String
) {
    /**
     * picks userId or displayname depending recipient
     * // TODO: let client track which UserInfo they belong to seperately
     *  // TODO: send back via SessionEvent.SetUserInfo -> client
     */
    fun getUserStringFor(session: UserSession) = if(user.userId == session.userId) user.userId else user.displayName

    fun toUserInfo(session: UserSession) = UserInfo(getUserStringFor(session), figureId)
}