package app

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

data class Valuation(
    val id: UUID,
    val group: ValuationGroup,
    val cause: Cause,
    val isSupporting: Boolean
)

object _Valuations {
    fun create(groupId: UUID, causeId: UUID, isSupporting: Boolean) {
        transaction {
            ValuationsTable.insert {
                it[id] = UUID.randomUUID()
                it[group] = groupId
                it[cause] = causeId
                it[is_supporting] = isSupporting
            }
        }
    }

    fun getUserValuations(userId: UUID): List<Valuation> {
        return transaction {
            ValuationsTable
                .leftJoin(CausesTable, { cause }, { id })
                .leftJoin(ValuationGroupsTable, { ValuationsTable.group }, { id })
                .leftJoin(ValuationGroupMembersTable, { ValuationGroupMembersTable.group }, { ValuationGroupsTable.id })
                .select { ValuationGroupMembersTable.member eq userId }
                .map { fromRow(it) }
        }
    }

    fun getUserValuationsForCauses(userId: UUID, causes: Iterable<UUID>): Map<UUID, Valuation> {
        return transaction {
            ValuationsTable
                .leftJoin(CausesTable, { cause }, { id })
                .leftJoin(ValuationGroupsTable, { ValuationsTable.group }, { id })
                .leftJoin(ValuationGroupMembersTable, { ValuationGroupMembersTable.group }, { ValuationGroupsTable.id })
                .select { (ValuationsTable.cause inList causes) and (ValuationGroupMembersTable.member eq userId) }
                .map { fromRow(it) }
                .associateBy { it.cause.id }
        }
    }

    fun fromRow(row: ResultRow): Valuation {
        return Valuation(
            row[ValuationsTable.id],
            DataLayer.ValuationGroups.fromRow(row),
            DataLayer.Causes.fromRow(row),
            row[ValuationsTable.is_supporting]
        )
    }
}
