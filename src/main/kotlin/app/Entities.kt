package app

import org.jetbrains.exposed.sql.Table

object UsersTable : Table() {
    val id = uuid("id").primaryKey()
    val password = text("password")
    val token = uuid("token").nullable()
    val role = integer("role")
    val notifications_count = integer("notifications_count")
}

object ClaimTypesTable : Table("claim_types") {
    val id = uuid("id").primaryKey()
    val name = text("name")
    val is_supporting = bool("is_supporting")
}

object ClaimTagsTable : Table("claim_tags") {
    val id = uuid("id").primaryKey()
    val name = text("name")
    val description = text("description").nullable()
}

object ClaimTagsReferencesTable : Table("claim_tag_references") {
    val tag_id = uuid("tag_id") references ClaimTagsTable.id
    val claim_id = uuid("claim_id") references ClaimsTable.id
}

object ClaimReasonsTable : Table("claim_reasons") {
    val id = uuid("id").primaryKey()
    val claim_type = uuid("claim_type") references ClaimTypesTable.id
    val name = text("name")
}

object ClaimsTable : Table("claims") {
    val id = uuid("id").primaryKey()
    val actor = uuid("actor") references PartiesTable.id
    val target = (uuid("target") references PartiesTable.id).nullable()
    val type = uuid("type") references ClaimTypesTable.id
    val cause = uuid("cause") references CausesTable.id
    val description = text("description").nullable()
    val source_ = text("source")
    val happened_at = date("happened_at")
    val created_at = datetime("created_at")
    val updated_at = datetime("updated_at").nullable()
    val moderation_status = integer("moderation_status")
}

object PartiesTable : Table("parties") {
    val id = uuid("id").primaryKey()
    val name = text("name")
}

object CausesTable : Table("causes") {
    val id = uuid("id").primaryKey()
    val name = text("name")
    val parent = (uuid("parent") references id).nullable()
}

object MonitoredPartiesTable : Table("monitored_parties") {
    val id = uuid("id").primaryKey()
    val watcher = uuid("watcher") references UsersTable.id
    val party = uuid("party") references PartiesTable.id
}

object ValuationGroupsTable : Table("valuation_groups") {
    val id = uuid("id").primaryKey()
    val name = text("name")
    val access_status = integer("access_status")
}

object ValuationGroupMembersTable : Table("valuation_group_members") {
    val group = (uuid("group") references ValuationGroupsTable.id).primaryKey()
    val member = (uuid("member") references UsersTable.id).primaryKey()
}

object ValuationsByGroupTable : Table("valuations_by_group") {
    val id = uuid("id").primaryKey()
    val group = uuid("group") references ValuationGroupsTable.id
    val cause = uuid("cause") references CausesTable.id
    val is_supporting = bool("is_supporting")
}

object ValuationsByUserTable : Table("valuations_by_user") {
    val id = uuid("id").primaryKey()
    val user = uuid("user") references UsersTable.id
    val cause = uuid("cause") references CausesTable.id
    val is_supporting = bool("is_supporting")
    val added_from = (uuid("added_from") references ValuationGroupsTable.id).nullable() // null means that it was added from causes list directly
}

object UserNotificationsTable : Table("user_notifications") {
    val id = uuid("id").primaryKey()
    val user = uuid("user") references UsersTable.id
    val message = text("message")
}
