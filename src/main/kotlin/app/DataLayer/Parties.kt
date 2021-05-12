package app

import org.jetbrains.exposed.sql.LowerCase
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

data class Party(
    val id: UUID,
    val name: String
)

object _Parties {
    fun create(party: Party) {
        return transaction {
            PartiesTable.insert {
                it[id] = party.id
                it[name] = party.name
            }
        }
    }

    fun delete(id: UUID) {
        return transaction {
            PartiesTable.deleteWhere { PartiesTable.id eq id }
        }
    }

    fun getById(id: UUID): Party {
        return transaction {
            PartiesTable.select({ PartiesTable.id eq id }).first()
        }.let {
            fromRow(it)
        }
    }

    fun getAll(): List<Party> {
        return transaction {
            PartiesTable.selectAll().map { fromRow(it) }
        }
    }

    fun query(name: String, limit: Int): List<Party> {
        return transaction {
            PartiesTable
                .select({ LowerCase(PartiesTable.name) like "%${name.toLowerCase()}%" })
                .limit(limit)
                .map { fromRow(it) }
        }
    }

    fun fromRow(row: ResultRow): Party {
        return Party(row[PartiesTable.id], row[PartiesTable.name])
    }
}
