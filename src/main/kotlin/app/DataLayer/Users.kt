package app

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.mindrot.jbcrypt.BCrypt
import java.util.*

data class User(
    val id: UUID,
    val passwordHash: String,
    val loginToken: UUID?,
    val role: USER_ROLES,
    val notificationsCount: Int
)

data class UserCreationResult(
    val username: String,
    val passwordPlainText: String,
    val loginToken: UUID
)

object _Users {
    fun create(): UserCreationResult {
        return transaction {
            val userId = UUID.randomUUID()
            val passwordPlainText = UUID.randomUUID()
            val loginToken = UUID.randomUUID()

            UsersTable.insert {
                it[id] = userId
                it[password] = BCrypt.hashpw("$passwordPlainText", BCrypt.gensalt())
                it[token] = loginToken
                it[role] = if (ENV.DEMO_MODE) USER_ROLES.ADMINISTRATOR.id else USER_ROLES.MEMBER.id
                it[notifications_count] = 0
            }

            UserCreationResult("$userId", "$passwordPlainText", loginToken)
        }
    }

    fun isPasswordValid(user: User, plainTextPassword: String): Boolean {
        return BCrypt.checkpw(plainTextPassword, user.passwordHash)
    }

    fun updateLoginToken(userId: UUID, token: UUID?) {
        return transaction {
            UsersTable.update({ UsersTable.id eq userId }) {
                it[UsersTable.token] = token
            }
        }
    }

    fun getById(id: UUID): User? {
        return transaction {
            UsersTable
                .select({ UsersTable.id eq id })
                .map { fromRow(it) }
                .firstOrNull()
        }
    }

    fun getByLoginToken(token: UUID): User? {
        return transaction {
            UsersTable
                .select({ UsersTable.token eq token })
                .map { fromRow(it) }
                .firstOrNull()
        }
    }

    fun fromRow(row: ResultRow): User {
        return User(
            row[UsersTable.id],
            row[UsersTable.password],
            row[UsersTable.token],
            USER_ROLES.fromId(row[UsersTable.role]),
            row[UsersTable.notifications_count]
        )
    }
}
