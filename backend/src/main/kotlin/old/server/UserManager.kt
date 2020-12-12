package old.server
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import old.server.db.User as DBUser
import org.jetbrains.exposed.sql.transactions.transaction
import old.server.db.Users
import old.server.db.connect
import old.server.db.findOrCreate

object UserManager {
    fun findDBUser(userId: String) = transaction(connect()) {
        addLogger(StdOutSqlLogger)
        DBUser.find(Users.userId eq userId).firstOrNull()
    }

    fun find(userId: String): User? {
        return findDBUser(userId)?.let {
            convert(it)
        }
    }

    fun convert(user: DBUser): User {
        return user.let {
            if(it.temporaryUser) {
                User.TemporaryUser(
                    userId = it.userId
                )
            } else {
                User.RegisteredUser(
                    userId = it.userId,
                    displayNameField = it.displayName,
                    passwordHash = it.passwordHash
                )
            }

        }
    }

    fun toDBUser(user: User) = transaction {
        addLogger(StdOutSqlLogger)
        when(user) {
            is User.RegisteredUser -> {
                findOrCreate(user.userId) {
                    passwordHash = user.passwordHash ?: error("null in passwordhash")
                    userId = user.userId
                    displayName = user.displayNameField
                    temporaryUser = false
                }
            }
            is User.TemporaryUser -> {
                findOrCreate(user.userId) {
                    passwordHash = null
                    userId = user.userId
                    displayName = null
                    temporaryUser = true
                }
            }
        }
    }
}