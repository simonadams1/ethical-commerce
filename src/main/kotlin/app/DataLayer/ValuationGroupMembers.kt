package app

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

data class ValuationGroupMember(
    val group: ValuationGroup,
    val member: User
)

object _ValuationGroupMembers {
    fun create(groupId: UUID, memberId: UUID) {
        transaction {
            ValuationGroupMembersTable.insert {
                it[group] = groupId
                it[member] = memberId
            }
        }
    }

    fun remove(groupId: UUID, memberId: UUID) {
        transaction {
            ValuationGroupMembersTable.deleteWhere { (ValuationGroupMembersTable.group eq groupId) and (ValuationGroupMembersTable.member eq memberId) }
        }
    }

    fun getGroupIdsForUser(userId: UUID): Set<UUID> {
        return transaction {
            ValuationGroupMembersTable.select { ValuationGroupMembersTable.member eq userId }.map { it[ValuationGroupMembersTable.group] }.toSet()
        }
    }

    fun fromRow(row: ResultRow): ValuationGroupMember {
        return ValuationGroupMember(
            DataLayer.ValuationGroups.fromRow(row),
            DataLayer.Users.fromRow(row)
        )
    }
}
