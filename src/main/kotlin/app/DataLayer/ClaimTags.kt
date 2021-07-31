package app

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

data class ClaimTag(
    val id: UUID,
    val name: String,
    val description: String
)

object _ClaimTags {
    fun get(claimId: UUID): List<ClaimTag> {
        return transaction {
            ClaimTagsReferencesTable
                .leftJoin(ClaimTagsTable)
                .select { ClaimTagsReferencesTable.claim_id eq claimId}
                .map { fromRow(it) }
        }
    }

    fun fromRow(row: ResultRow): ClaimTag {
        return ClaimTag(
            row[ClaimTagsTable.id],
            row[ClaimTagsTable.name],
            row[ClaimTagsTable.description] ?: ""
        )
    }
}
