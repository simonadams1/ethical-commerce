package app

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import app.ClaimsTable.type
import org.joda.time.DateTime
import java.util.*

data class Claim(
    val id: UUID,
    val actor: Party,
    val target: Party?,
    val type: ClaimType,
    val cause: Cause,
    val description: String?,
    val source: String,
    val happened_at: DateTime,
    val created_at: DateTime,
    val updated_at: DateTime?,
    val moderation_status: MODERATION_STATUS
)

fun queryClaims(doSelect: ((join: Join) -> Query)): List<Claim> {
    val partiesAliasActor = PartiesTable.alias("parties_actor")
    val partiesAliasTarget = PartiesTable.alias("parties_target")

    return transaction {
        val join = ClaimsTable
            .leftJoin(partiesAliasActor, { actor }, { partiesAliasActor[PartiesTable.id] })
            .leftJoin(partiesAliasTarget, { ClaimsTable.target }, { partiesAliasTarget[PartiesTable.id] })
            .leftJoin(ClaimTypesTable, { type }, { id })
            .leftJoin(CausesTable, { ClaimsTable.cause }, { id })

        doSelect(join)
            .map {
                Claim(
                    it[ClaimsTable.id],
                    Party(
                        it[partiesAliasActor[PartiesTable.id]],
                        it[partiesAliasActor[PartiesTable.name]]
                    ),

                    if (it[ClaimsTable.target] == null) {
                        null
                    } else {
                        Party(
                            it[partiesAliasTarget[PartiesTable.id]],
                            it[partiesAliasTarget[PartiesTable.name]]
                        )
                    },

                    DataLayer.ClaimTypes.fromRow(it),
                    Cause(it[CausesTable.id], it[CausesTable.name], null),
                    it[ClaimsTable.description],
                    it[ClaimsTable.source_],
                    it[ClaimsTable.happened_at],
                    it[ClaimsTable.created_at],
                    it[ClaimsTable.updated_at],
                    MODERATION_STATUS.fromId(it[ClaimsTable.moderation_status])
                )
            }
    }
}

object _Claims {
    fun getSome(from: Int, to: Int): List<Claim> {
        return queryClaims({
            it.select { ClaimsTable.moderation_status eq MODERATION_STATUS.APPROVED.id }
                .orderBy(ClaimsTable.created_at)
                .limit(to, offset = from)
        })
    }

    fun getCount(): Int {
        return queryClaims({
            it.select { ClaimsTable.moderation_status eq MODERATION_STATUS.APPROVED.id }
        }).count()
    }

    fun getForModeration(): List<Claim> {
        return queryClaims({
            it.select { ClaimsTable.moderation_status eq MODERATION_STATUS.PENDING.id }
                .orderBy(ClaimsTable.created_at)
                .limit(100)
        })
    }

    fun getById(id: UUID): Claim {
        return queryClaims({
            it.select({ ClaimsTable.id eq id })
        }).first()
    }

    fun getByParty(partyId: UUID): List<Claim> {
        return queryClaims({
            it.select({ (ClaimsTable.actor eq partyId) or (ClaimsTable.target eq partyId) })
        })
    }

    fun delete(id: UUID) {
        transaction {
            ClaimTagsReferencesTable.deleteWhere { ClaimTagsReferencesTable.claim_id eq id }
            ClaimsTable.deleteWhere { ClaimsTable.id eq id }
        }
    }

    fun setModerationStatus(id: UUID, status: MODERATION_STATUS) {
        transaction {
            ClaimsTable.update({ ClaimsTable.id eq id }) {
                it[moderation_status] = status.id
            }
        }
    }

    fun createOrUpdate(
        actor: String,
        target: String,
        type: UUID,
        cause: String,
        source: String,
        tags: List<String>,
        description: String?,
        happened_at: DateTime,
        itemToUpdate: UUID? = null,
        skipModerationQueue: Boolean = false
    ): UUID {
        return transaction {
            val actorEntity = PartiesTable.select({ PartiesTable.name eq actor }).firstOrNull()
            val causeEntity = CausesTable.select({ CausesTable.name eq cause }).firstOrNull()

            val actorId: UUID
            val targetId: UUID?
            val causeId: UUID

            if (actorEntity == null) {
                val newActorId = UUID.randomUUID()

                PartiesTable.insert {
                    it[id] = newActorId
                    it[name] = actor
                }

                actorId = newActorId
            } else {
                actorId = actorEntity[PartiesTable.id]
            }

            if (target.isEmpty()) {
                targetId = null
            } else {
                val targetEntity = PartiesTable.select({ PartiesTable.name eq target }).firstOrNull()

                if (targetEntity == null) {
                    val newTargetId = UUID.randomUUID()

                    PartiesTable.insert {
                        it[id] = newTargetId
                        it[name] = target
                    }

                    targetId = newTargetId
                } else {
                    targetId = targetEntity[PartiesTable.id]
                }
            }

            if (causeEntity == null) {
                val newCauseId = UUID.randomUUID()

                CausesTable.insert {
                    it[id] = newCauseId
                    it[name] = cause
                }

                causeId = newCauseId
            } else {
                causeId = causeEntity[CausesTable.id]
            }

            val claimId: UUID

            if (itemToUpdate == null) {
                val newClaimId = UUID.randomUUID()

                claimId = newClaimId

                ClaimsTable.insert {
                    it[id] = newClaimId
                    it[ClaimsTable.actor] = actorId
                    it[ClaimsTable.target] = targetId
                    it[ClaimsTable.type] = type
                    it[ClaimsTable.cause] = causeId
                    it[ClaimsTable.description] = description
                    it[source_] = source
                    it[ClaimsTable.happened_at] = happened_at
                    it[created_at] = DateTime.now()
                    it[moderation_status] = if (skipModerationQueue) MODERATION_STATUS.APPROVED.id else MODERATION_STATUS.PENDING.id
                }
            } else {
                claimId = itemToUpdate

                ClaimsTable.update({ ClaimsTable.id eq itemToUpdate }) {
                    it[ClaimsTable.actor] = actorId
                    it[ClaimsTable.target] = targetId
                    it[ClaimsTable.type] = type
                    it[ClaimsTable.cause] = causeId
                    it[ClaimsTable.description] = description
                    it[source_] = source
                    it[ClaimsTable.happened_at] = happened_at
                }
            }

            // HANDLE TAGS

            val tagsThatAlreadyExist = ClaimTagsTable
                .select { ClaimTagsTable.name inList tags }
                .map { DataLayer.ClaimTags.fromRow(it) }

            var tagIdsForClaim = tagsThatAlreadyExist.map { it.id }

            val namesOfTagsThatAlreadyExist = tagsThatAlreadyExist.map { it.name }.toSet()
            val tagsToCreate = tags.filter { !namesOfTagsThatAlreadyExist.contains(it) }

            for (tagName in tagsToCreate) {
                val newId = UUID.randomUUID()

                tagIdsForClaim = tagIdsForClaim.plus(newId)

                ClaimTagsTable.insert {
                    it[id] = newId
                    it[name] = tagName
                }
            }

            // remove existing tags to make editing easier
            ClaimTagsReferencesTable.deleteWhere { ClaimTagsReferencesTable.claim_id eq claimId }

            for (id in tagIdsForClaim) {
                ClaimTagsReferencesTable.insert {
                    it[claim_id] = claimId
                    it[tag_id] = id
                }
            }

            return@transaction claimId
        }
    }
}
