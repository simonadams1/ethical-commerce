package app

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

data class ValuationGroup(
    val id: UUID,
    val name: String,
    val accessStatus: ACCESS_STATUS,
    val ownerUserId: UUID
)

object _ValuationGroups {
    fun canAdministerGroup(group: ValuationGroup, userId: UUID): Boolean {
        return group.ownerUserId == userId
    }

    fun create(nameValue: String, accessStatus: ACCESS_STATUS, ownerUserId: UUID) {
        transaction {
            ValuationGroupsTable.insert {
                it[id] = UUID.randomUUID()
                it[name] = nameValue
                it[access_status] = accessStatus.id
                it[owner] = ownerUserId
            }
        }
    }

    fun delete(id: UUID) {
        transaction {
            ValuationGroupsTable.update({ ValuationGroupsTable.id eq id }) {
                it[access_status] = ACCESS_STATUS.PUBLIC_DELISTED.id
            }
        }
    }

    fun getPublic(): List<ValuationGroup> {
        return transaction {
            ValuationGroupsTable.select({ ValuationGroupsTable.access_status eq ACCESS_STATUS.PUBLIC.id }).map { fromRow(it) }
        }
    }

    fun getOne(groupId: UUID): ValuationGroup {
        return transaction {
            ValuationGroupsTable.select({ ValuationGroupsTable.id eq groupId }).map { fromRow(it) }.first()
        }
    }

    fun query(name: String, limit: Int): List<ValuationGroup> {
        return transaction {
            ValuationGroupsTable
                .select({ LowerCase(ValuationGroupsTable.name) like "%${name.toLowerCase()}%" })
                .limit(limit)
                .map { fromRow(it) }
        }
    }

    fun fromRow(row: ResultRow): ValuationGroup {
        return ValuationGroup(
            row[ValuationGroupsTable.id],
            row[ValuationGroupsTable.name],
            ACCESS_STATUS.fromId(row[ValuationGroupsTable.access_status]),
            row[ValuationGroupsTable.owner]
        )
    }
}
