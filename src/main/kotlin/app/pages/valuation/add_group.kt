package app.pages.valuation

import app.*
import io.javalin.http.Context
import app.pages.Body
import app.pages.Head
import app.pages.Page
import app.pages.errorPage
import app.ui_components.FormGroup
import kotlinx.html.*

fun addGroupPage(ctx: Context) {
    ctx.html(
        Page {
            Head {
                title {
                    + gettext("Add valuation group")
                }
            }

            Body(ctx) {
                h1 {
                    + gettext("Add valuation group")
                }

                form {
                    method = FormMethod.post
                    action = "${Urls.Valuation.valuationGroupAdd}"

                    FormGroup(gettext("Name")) {
                        input {
                            type = InputType.text
                            name = groupNameField
                            required = true
                            classes = setOf("form-control")
                        }
                    }

                    div {
                        button {
                            type = ButtonType.submit
                            classes = setOf("btn", "btn-primary")

                            + gettext("Create")
                        }
                    }
                }
            }
        }
    )
}


fun handleAddValuationGroup(ctx: Context) {
    val name = ctx.formParam(groupNameField)
    val currentUser = Helpers.getUserFromContext(ctx)

    if (name == null || currentUser == null) {
        ctx.html(errorPage(ctx))
        return
    }

    DataLayer.ValuationGroups.create(name, ACCESS_STATUS.PUBLIC, currentUser.id)

    ctx.redirect("${Urls.Valuation.viewGroups}")
}
