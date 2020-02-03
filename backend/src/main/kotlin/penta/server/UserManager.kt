package penta.server
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.addLogger
import penta.server.db.User as DBUser
import org.jetbrains.exposed.sql.transactions.transaction
import penta.server.db.Users
import penta.server.db.findOrCreate

object UserManager {
    fun findDBUser(userId: String) = transaction {
        addLogger(Slf4jSqlDebugLogger)
        DBUser.find(Users.userId eq userId).firstOrNull()
    }

    fun find(userId: String): User.RegisteredUser? {
        val user = findDBUser(userId)
        return user?.let {
            User.RegisteredUser(
                userId = it.userId,
                displayNameField = it.displayName,
                passwordHash = it.passwordHash
            )
        }
    }

    fun toDBUser(user: User.RegisteredUser) = transaction {
        addLogger(Slf4jSqlDebugLogger)
        findOrCreate(user.userId) {
            passwordHash = user.passwordHash ?: error("null in passwordhash")
            userId = user.userId
            displayName = user.displayNameField
        }
    }

}