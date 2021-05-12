package app

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

data class ClaimType(
    val id: UUID,
    val name: String,
    val isSupporting: Boolean
)

object _ClaimTypes {
    fun create(claimType: ClaimType) {
        return transaction {
            ClaimTypesTable.insert {
                it[id] = claimType.id
                it[name] = claimType.name
                it[is_supporting] = claimType.isSupporting
            }
        }
    }

    fun getById(id: UUID): ClaimType {
        return transaction {
            ClaimTypesTable.select({ PartiesTable.id eq id }).first()
        }.let {
            fromRow(it)
        }
    }

    fun getAll(): List<ClaimType> {
        return transaction {
            ClaimTypesTable.selectAll().orderBy(ClaimTypesTable.name).map { fromRow(it) }
        }
    }

    fun getCount(): Int {
        return transaction {
            ClaimTypesTable.selectAll().count()
        }
    }

    fun fromRow(row: ResultRow): ClaimType {
        return ClaimType(row[ClaimTypesTable.id], row[ClaimTypesTable.name], row[ClaimTypesTable.is_supporting])
    }
}
