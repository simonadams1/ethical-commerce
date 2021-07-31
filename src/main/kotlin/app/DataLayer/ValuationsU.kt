package app

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*


data class ValuationU(
    val id: UUID,
    val user: User,
    val cause: Cause,
    val isSupporting: Boolean
)

object _ValuationsU {
    fun create(userId: UUID, causeId: UUID, isSupporting: Boolean, addedFromGroup: UUID?) {
        transaction {
            ValuationsByUserTable.insert {
                it[id] = UUID.randomUUID()
                it[user] = userId
                it[cause] = causeId
                it[is_supporting] = isSupporting
                it[added_from] = addedFromGroup
            }
        }
    }

    fun create(userId: UUID, valuationsG: List<ValuationG>) {
        transaction {
            for (valuationG in valuationsG) {
                transaction {
                    ValuationsByUserTable.insert {
                        it[id] = UUID.randomUUID()
                        it[user] = userId
                        it[cause] = valuationG.cause.id
                        it[is_supporting] = valuationG.isSupporting
                        it[added_from] = valuationG.group.id
                    }
                }
            }
        }
    }

    fun update(valuationId: UUID, isSupporting: Boolean) {
        transaction {
            ValuationsByUserTable.update({ValuationsByUserTable.id eq valuationId}) {
                it[is_supporting] = isSupporting
            }
        }
    }

    fun delete(id: UUID) {
        transaction {
            ValuationsByUserTable.deleteWhere { ValuationsByUserTable.id eq id }
        }
    }

    fun getUserValuations(userId: UUID): List<ValuationU> {
        return transaction {
            ValuationsByUserTable
                .leftJoin(CausesTable, { cause }, { id })
                .leftJoin(UsersTable, { ValuationsByUserTable.user }, { id })
                .select { ValuationsByUserTable.user eq userId }
                .map { fromRow(it) }
        }
    }

    fun getUserValuationsForCauses(user: User, causes: Iterable<UUID>): Map<UUID, ValuationU> {
        return transaction {
            ValuationsByUserTable
                .leftJoin(CausesTable, { cause }, { id })
                .select { (ValuationsByUserTable.user eq user.id) and (ValuationsByUserTable.cause inList causes) }
                .map { fromRow(it, user) }
                .associateBy { it.cause.id }
        }
    }

    fun fromRow(row: ResultRow): ValuationU {
        return ValuationU(
            row[ValuationsByUserTable.id],
            DataLayer.Users.fromRow(row),
            DataLayer.Causes.fromRow(row),
            row[ValuationsByUserTable.is_supporting]
        )
    }

    fun fromRow(row: ResultRow, user: User): ValuationU {
        return ValuationU(
            row[ValuationsByUserTable.id],
            user,
            DataLayer.Causes.fromRow(row),
            row[ValuationsByUserTable.is_supporting]
        )
    }
}
