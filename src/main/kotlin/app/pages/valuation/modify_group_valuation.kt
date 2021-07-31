package app.pages.valuation

import app.*
import app.pages.*
import app.ui_components.FormGroup
import io.javalin.http.Context
import kotlinx.html.*
import java.util.*

fun addValuationItemToGroupPage(ctx: Context) {
    val user = Helpers.getUserFromContext(ctx)
    val valuationGroupId = Helpers.parseUUID(ctx.pathParam(valuationGroupPlaceholder))

    if (user == null || valuationGroupId == null) {
        ctx.html(errorPage(ctx))
        return
    }

    val group = DataLayer.ValuationGroups.getOne(valuationGroupId)

    ctx.html(
        Page {
            Head {
                title {
                    + gettext("Add valuation item")
                }
            }

            Body(ctx) {
                h1 {
                    + gettext("Add valuation item")
                }

                form {
                    method = FormMethod.post
                    action = "${Urls.Valuation.addValuationToGroup(group.id.toString())}"

                    FormGroup(gettext("Cause")) {
                        SelectFromRemote(
                            app.pages.causes.Urls.Causes.search,
                            causeFieldName
                        )
                    }

                    div("form-check") {
                        label {
                            input {
                                type = InputType.radio
                                name = supportingFieldName
                                value = supportingFieldValue
                                required = true
                                classes = setOf("form-check-input")
                            }

                            + gettext("Supporting")
                        }
                    }

                    div("form-check") {
                        label {
                            input {
                                type = InputType.radio
                                name = supportingFieldName
                                required = true
                                classes = setOf("form-check-input")
                            }

                            + gettext("Opposing")
                        }
                    }

                    br

                    button {
                        type = ButtonType.submit
                        classes = setOf("btn", "btn-primary")

                        + gettext("Add")
                    }
                }
            }
        }
    )
}

fun handleAddValuationItemToGroup(ctx: Context) {
    val user = Helpers.getUserFromContext(ctx)
    val causeId = UUID.fromString(ctx.formParam(causeFieldName))
    val groupId = UUID.fromString(ctx.pathParam(valuationGroupPlaceholder))
    val group = DataLayer.ValuationGroups.getOne(groupId)

    val isPositive = ctx.formParam(supportingFieldName) == supportingFieldValue

    if (user == null || !DataLayer.ValuationGroups.canAdministerGroup(group, user.id)) {
        ctx.html(errorPage(ctx))
        return
    }

    DataLayer.ValuationsG.create(groupId, causeId, isPositive)

    ctx.redirect("${Urls.Valuation.singleGroup(groupId.toString())}")
}

fun handleRemoveValuationItemFromGroup(ctx: Context) {
    val user = Helpers.getUserFromContext(ctx)
    val groupId = UUID.fromString(ctx.pathParam(valuationGroupPlaceholder))
    val valuationGId = UUID.fromString(ctx.pathParam(valuationGPlaceholder))
    val group = DataLayer.ValuationGroups.getOne(groupId)

    if (
        user == null ||
        !DataLayer.ValuationGroups.canAdministerGroup(group, user.id) ||
        valuationGId == null
    ) {
        ctx.html(errorPage(ctx))
        return
    }

    DataLayer.ValuationsG.delete(valuationGId)

    ctx.redirect("${Urls.Valuation.singleGroup(groupId.toString())}")
}
