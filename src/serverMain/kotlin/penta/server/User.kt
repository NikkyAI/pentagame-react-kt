package penta.server

sealed class User {
    abstract val userId: String
    abstract val displayName: String

    data class RegisteredUser(
        override val userId: String,
        var displayNameProperty: String? = null,
        var passwordHash: String? = null
    ): User() {
        override val displayName: String
            get() = displayNameProperty ?: userId
    }

    data class TemporaryUser(
        override val userId: String
    ): User() {
        override val displayName: String
            get() = "guest_$userId"
    }

}