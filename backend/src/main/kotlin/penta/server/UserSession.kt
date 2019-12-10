package penta.server

data class UserSession(
    val userId: String
) {
    fun asUser(): User {
        // TODO: retreive user from db
        return User.TemporaryUser(userId)
    }
}
