package app.pages.valuation

import io.javalin.http.Context
import kotlinx.html.*
import app.ACCESS_STATUS
import app.gettext
import app.pages.Body
import app.pages.Head
import app.pages.Page
import app.DataLayer
import app.Helpers
import app.pages.errorPage

val groupNameField = "name"
val membershipActionField = "membership"
val membershipGroupIdField = "group"

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
                h1 {
                    + gettext("All groups")
                }

                p {
                    + gettext("If you subscribe to updates, you will receive a notification when items are added or removed from a group. It does not automatically affect your valuations.")
                }

                table {
                    classes = setOf("app-table")

                    thead {
                        tr {
                            th { + gettext("Name") }
                            th { + gettext("Status") }
                        }
                    }

                    tbody {
                        for (group in allGroups) {
                            tr {
                                td {
                                    a {
                                        href = Urls.Valuation.singleGroup(group.id.toString()).toString()
                                        + group.name
                                    }
                                }
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
                        }
                    }
                }

                br
                br

                h3 {
                    + gettext("Add a public valuation group")
                }

                form {
                    method = FormMethod.post
                    action = "${Urls.Valuation.valuationGroupAdd}"

                    label {
                        + gettext("Name")

                        input {
                            type = InputType.text
                            name = groupNameField
                            required = true
                        }
                    }

                    div {
                        input {
                            type = InputType.submit
                            value = gettext("Submit")
                        }
                    }
                }
            }
        }
    )
}

fun handleAddValuationGroup(ctx: Context) {
    val name = ctx.formParam(groupNameField)

    if (name == null) {
        ctx.html(errorPage(ctx))
        return
    }

    DataLayer.ValuationGroups.create(name, ACCESS_STATUS.PUBLIC)

    ctx.redirect("${Urls.Valuation.viewGroups}")
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
