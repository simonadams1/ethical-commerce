package app.pages.valuation

import io.javalin.http.Context
import kotlinx.html.*
import app.gettext
import app.pages.Body
import app.pages.Head
import app.pages.Page
import app.DataLayer
import app.Helpers
import app.pages.errorPage
import app.ui_components.BUTTON_STYLE
import app.ui_components.FlexBlock
import app.ui_components.LinkButton
import java.util.*

val groupNameField = "name"
val membershipActionField = "membership"
val membershipGroupIdField = "group"

val subscriptionsImplemented = false

fun valuationGroupsView(ctx: Context) {
    val currentUser = Helpers.getUserFromContext(ctx)
    val allGroups = DataLayer.ValuationGroups.getPublic()

    if (currentUser == null) {
        ctx.html(errorPage(ctx))
        return
    }

    val userGroups = DataLayer.ValuationGroupMembers.getGroupIdsForUser(currentUser.id)

    ctx.html(
        Page {
            Head {
                title {
                    + gettext("Valuation groups")
                }
            }

            Body(ctx) {
                FlexBlock {
                    h1 {
                        + gettext("All groups")
                    }

                    LinkButton(
                        gettext("Add new"),
                        Urls.Valuation.valuationGroupAdd,
                        BUTTON_STYLE.PRIMARY
                    )
                }

                p {
                    + gettext("If you subscribe to updates, you will receive a notification when items are added or removed from a group. It does not automatically affect your valuations.")
                }

                table {
                    classes = setOf("app-table")

                    thead {
                        tr {
                            th { + gettext("Name") }

                            if (subscriptionsImplemented) {
                                th { + gettext("Updates") }
                            }

                            th { + gettext("Actions") }
                        }
                    }

                    tbody {
                        for (group in allGroups) {
                            val canAdministerGroup = DataLayer.ValuationGroups.canAdministerGroup(group, currentUser.id)

                            tr {
                                td {
                                    a {
                                        href = Urls.Valuation.singleGroup(group.id.toString()).toString()
                                        + group.name
                                    }
                                }

                                if (subscriptionsImplemented) {
                                    td {
                                        val isMember = userGroups.contains(group.id)

                                        form {
                                            method = FormMethod.post
                                            action = "${Urls.Valuation.valuationGroupChangeMemberShipStatus}"

                                            input {
                                                type = InputType.hidden
                                                name = membershipActionField
                                                value = if (isMember) "0" else "1"
                                            }

                                            input {
                                                type = InputType.hidden
                                                name = membershipGroupIdField
                                                value = "${group.id}"
                                            }

                                            button {
                                                type = ButtonType.submit

                                                if (isMember) {
                                                    + gettext("Unsubscribe from updates")
                                                } else {
                                                    + gettext("Subscribe to updates")
                                                }
                                            }
                                        }
                                    }
                                }

                                td {
                                    if (canAdministerGroup) {
                                        form {
                                            method = FormMethod.post
                                            action = "${Urls.Valuation.deleteGroup(group.id.toString())}"

                                            button {
                                                type = ButtonType.submit
                                                classes = setOf("btn btn-sm btn-outline-danger")

                                                + gettext("Delete group")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

fun handleMembershipStatusChange(ctx: Context) {
    val user = Helpers.getUserFromContext(ctx)
    val groupIdString = ctx.formParam(membershipGroupIdField)
    val membershipAction = ctx.formParam(membershipActionField)

    if (membershipAction == null || groupIdString == null) {
        ctx.html(errorPage(ctx))
        return
    }

    val groupId = Helpers.parseUUID(groupIdString)

    if (groupId == null || user == null) {
        ctx.html(errorPage(ctx))
        return
    }

    if (membershipAction == "1") {
        DataLayer.ValuationGroupMembers.create(groupId, user.id)
    } else {
        DataLayer.ValuationGroupMembers.remove(groupId, user.id)
    }

    ctx.redirect("${Urls.Valuation.viewGroups}")
}

fun handleDeleteGroup(ctx: Context) {
    val user = Helpers.getUserFromContext(ctx)
    val groupId = UUID.fromString(ctx.pathParam(valuationGroupPlaceholder))
    val group = DataLayer.ValuationGroups.getOne(groupId)

    if (groupId == null || user == null || !DataLayer.ValuationGroups.canAdministerGroup(group, user.id)) {
        ctx.html(errorPage(ctx))
        return
    }

    DataLayer.ValuationGroups.delete(groupId)

    ctx.redirect("${Urls.Valuation.viewGroups}")
}
