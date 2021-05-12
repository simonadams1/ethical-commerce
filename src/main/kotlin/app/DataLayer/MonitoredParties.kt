package app

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

data class MonitoriedParties(
    val id: UUID,
    val watcher: User,
    val party: Party
)

object _MonitoredParties {
    fun create(partyName: String, userId: UUID) {
        transaction {
            val partyEntity = PartiesTable.select({ PartiesTable.name eq partyName }).firstOrNull()
            var partyId: UUID

            if (partyEntity == null) {
                val newPartyId = UUID.randomUUID()

                PartiesTable.insert {
                    it[id] = newPartyId
                    it[name] = partyName
                }

                partyId = newPartyId
            } else {
                partyId = partyEntity[PartiesTable.id]
            }

            MonitoredPartiesTable.insert {
                it[id] = UUID.randomUUID()
                it[watcher] = userId
                it[party] = partyId
            }
        }
    }

    fun getAll(userId: UUID): List<MonitoriedParties> {
        return transaction {
            MonitoredPartiesTable
                .leftJoin(UsersTable, { watcher }, { id })
                .leftJoin(PartiesTable, { MonitoredPartiesTable.party }, { id })
                .select { MonitoredPartiesTable.watcher eq userId }
                .map {
                    MonitoriedParties(
                        it[MonitoredPartiesTable.id],
                        DataLayer.Users.fromRow(it),
                        DataLayer.Parties.fromRow(it)
                    )
                }
        }
    }
}
