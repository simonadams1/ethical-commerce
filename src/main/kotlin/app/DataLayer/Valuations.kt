package app

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

data class ValuationG(
    val id: UUID,
    val group: ValuationGroup,
    val cause: Cause,
    val isSupporting: Boolean
)

object _ValuationsG {
    fun create(groupId: UUID, causeId: UUID, isSupporting: Boolean) {
        transaction {
            ValuationsByGroupTable.insert {
                it[id] = UUID.randomUUID()
                it[group] = groupId
                it[cause] = causeId
                it[is_supporting] = isSupporting
            }
        }
    }

    fun getValuationsGU(user: User, group_id: UUID): List<Pair<ValuationG, ValuationU?>> {
        return transaction {
            ValuationsByGroupTable
                .leftJoin(CausesTable, { cause }, { id })
                .leftJoin(ValuationGroupsTable, { ValuationsByGroupTable.group }, { id })
                .leftJoin(ValuationsByUserTable, { ValuationsByUserTable.cause }, { ValuationsByGroupTable.cause })
                .select { ValuationsByGroupTable.group eq group_id }
                .map {
                    Pair(
                        fromRow(it),
                        /*
                        * IDE is wrong! It is not always true!
                        * If user doesn't have valuation for the same cause as in the group,
                        * it will be null.
                        */
                        @Suppress("SENSELESS_COMPARISON")
                        if (it[ValuationsByUserTable.id] == null) {
                            null
                        } else {
                            DataLayer.ValuationsU.fromRow(it, user)
                        }
                    )
                }
        }
    }

    fun getValuationsG(group_id: UUID): List<ValuationG> {
        return transaction {
            ValuationsByGroupTable
                .leftJoin(CausesTable, { cause }, { id })
                .leftJoin(ValuationGroupsTable, { ValuationsByGroupTable.group }, { id })
                .select { ValuationsByGroupTable.group eq group_id }
                .map { fromRow(it) }
        }
    }

    fun fromRow(row: ResultRow): ValuationG {
        return ValuationG(
            row[ValuationsByGroupTable.id],
            DataLayer.ValuationGroups.fromRow(row),
            DataLayer.Causes.fromRow(row),
            row[ValuationsByGroupTable.is_supporting]
        )
    }
}
