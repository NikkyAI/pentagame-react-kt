package old.server

sealed class User {
    abstract val userId: String
    abstract val displayName: String

    data class RegisteredUser(
        override val userId: String,
        var displayNameField: String? = null,
        var passwordHash: String? = null
    ) : User() {
        override val displayName: String
            get() = displayNameField ?: userId
    }

    data class TemporaryUser(
        override val userId: String
    ) : User() {
        override val displayName: String
            get() = "guest_$userId"
    }
}