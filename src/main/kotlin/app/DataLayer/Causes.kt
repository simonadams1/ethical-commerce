package app

import org.jetbrains.exposed.sql.LowerCase
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
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

    fun getAll(): List<Cause> {
        return transaction {
            CausesTable.selectAll().map { fromRow(it) }
        }
    }

    fun getCount(): Int {
        return transaction {
            CausesTable.selectAll().count()
        }
    }

    fun query(name: String, limit: Int): List<Party> {
        return transaction {
            CausesTable.select({ LowerCase(CausesTable.name) like "%${name.toLowerCase()}%" }).limit(limit).map {
                Party(it[CausesTable.id], it[CausesTable.name])
            }
        }
    }

    fun fromRow(row: ResultRow): Cause {
        return Cause(row[CausesTable.id], row[CausesTable.name], null)
    }
}
