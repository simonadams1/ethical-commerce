package app

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import app.pages.claims.Urls
import java.util.*

data class UserNotification(
    val id: UUID,
    val user: User,
    val message: String
)

object _UserNotifications {
    fun delete(userId: UUID, notificationId: UUID) {
        transaction {
            UserNotificationsTable.deleteWhere { (UserNotificationsTable.id eq notificationId) and (UserNotificationsTable.user eq userId) }

            UsersTable.update({ UsersTable.id eq userId }) {
                with(SqlExpressionBuilder) {
                    it.update(notifications_count, notifications_count - 1)
                }
            }
        }
    }

    fun create(claimId: UUID) {
        transaction {
            val claim = DataLayer.Claims.getById(claimId)
            var partiesIds = setOf(claim.actor.id)

            if (claim.target != null) {
                partiesIds = partiesIds.plus(claim.target.id)
            }

            val userIds = MonitoredPartiesTable
                .select { MonitoredPartiesTable.party inList partiesIds }
                .map { it[MonitoredPartiesTable.watcher] }

            for (userId in userIds) {
                // would be good to do this in a single insert
                UserNotificationsTable.insert {
                    it[id] = UUID.randomUUID()
                    it[user] = userId
                    it[message] = gettext("New claim from a party you are subscribed to: ${Urls.Claims.singleClaim("$claimId")}")
                }

                UsersTable.update({ UsersTable.id eq userId }) {
                    with(SqlExpressionBuilder) {
                        it.update(notifications_count, notifications_count + 1)
                    }
                }
            }
        }
    }

    fun getAll(userId: UUID): List<UserNotification> {
        return transaction {
            UserNotificationsTable
                .leftJoin(UsersTable, { user }, { id })
                .select { UserNotificationsTable.user eq userId }
                .map { fromRow(it) }
        }
    }

    fun fromRow(row: ResultRow): UserNotification {
        return UserNotification(
            row[UserNotificationsTable.id],
            DataLayer.Users.fromRow(row),
            row[UserNotificationsTable.message]
        )
    }
}
