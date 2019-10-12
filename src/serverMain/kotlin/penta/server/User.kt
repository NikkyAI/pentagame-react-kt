package penta.server

data class User(
    val hashId: String,
    var name: String? = null,
    var passwordHash: String? = null
) {
    val displayName: String = name ?: "noob_"+hashId.substring(0, 5)
}