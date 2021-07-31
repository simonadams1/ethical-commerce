package app

import org.jetbrains.exposed.sql.ResultRow
import java.util.*

data class ClaimTag(
    val id: UUID,
    val name: String,
    val description: String
)

object _ClaimTags {
    fun fromRow(row: ResultRow): ClaimTag {
        return ClaimTag(
            row[ClaimTagsTable.id],
            row[ClaimTagsTable.name],
            row[ClaimTagsTable.description] ?: ""
        )
    }
}
