package app

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

data class Cause(
    val id: UUID,
    val name: String,
    val parent: Cause?
)

object _Causes {
    fun create(cause: Cause) {
        return transaction {
            CausesTable.insert {
                it[id] = cause.id
                it[name] = cause.name
            }
        }
    }

    fun getById(id: UUID): Cause {
        return transaction {
            CausesTable.select({ CausesTable.id eq id }).first()
        }.let { fromRow(it) }
    }

    fun query(user: User, from: Int, to: Int): List<Pair<Cause, ValuationU?>> {
        return transaction {
            CausesTable
                .leftJoin(ValuationsByUserTable)
                .selectAll()
                .limit(to, offset = from)
                .map {
                    val cause = fromRow(it)
                    /*
                    * IDE is wrong! It is not always true!
                    * If user doesn't have a valuation for the cause, it will be null
                    */
                    @Suppress("SENSELESS_COMPARISON")
                    val valuationU = if (it[ValuationsByUserTable.id] == null) {
                        null
                    } else {
                        ValuationU(
                            it[ValuationsByUserTable.id],
                            user,
                            cause,
                            it[ValuationsByUserTable.is_supporting]
                        )
                    }

                    Pair(cause, valuationU)
                }
        }
    }

    fun getCount(): Int {
        return transaction {
            CausesTable.selectAll().count()
        }
    }

    fun query(user: User, name: String, limit: Int): List<Pair<Cause, ValuationU?>> {
        return transaction {
            CausesTable
                .leftJoin(ValuationsByUserTable)
                .select({ LowerCase(CausesTable.name) like "%${name.toLowerCase()}%" and (ValuationsByUserTable.user eq user.id) })
                .limit(limit)
                .map {
                    val cause = fromRow(it)
                    val valuationU = ValuationU(
                        it[ValuationsByUserTable.id],
                        user,
                        cause,
                        it[ValuationsByUserTable.is_supporting]
                    )

                    Pair<Cause, ValuationU?>(cause, valuationU)
                }
        }
    }

    fun fromRow(row: ResultRow): Cause {
        return Cause(row[CausesTable.id], row[CausesTable.name], null)
    }
}
