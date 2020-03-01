package penta.server.db

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

object Users: UUIDTable() {
    val userId = varchar("userId", 50)
        .uniqueIndex()
    val passwordHash = varchar("passwordHash", 50)
        .nullable()
    val displayName = varchar("displayName", 50)
        .nullable()
    val temporaryUser = bool("temporaryUser")

    init {
//        displayName.defaultValueFun = { null }
    }
}

class User(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<User>(Users)

    var userId by Users.userId
    var passwordHash by Users.passwordHash
    var displayName by Users.displayName
    var players by Game via PlayingUsers
    var temporaryUser by Users.temporaryUser

    override fun toString(): String {
        return transaction {
            "User(id=$id, userId=$userId, passwordHash=$passwordHash, displayName=$displayName, players=${players.map { it }})"
        }
    }
}

fun Transaction.findOrCreate(
    userId: String,
    builder: User.() -> Unit
): User {
    return User.find {
        Users.userId eq userId
    }.firstOrNull()
        ?: let {
            println("creating user")
            User.new {
                this.userId = userId
                this.builder()
            }
        }
}