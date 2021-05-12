package app.pages.claim_types

import io.javalin.http.Context
import kotlinx.html.*
import app.ClaimType
import app.DataLayer
import app.gettext
import app.pages.Body
import app.pages.Head
import app.pages.Page
import app.pages.errorPage
import java.util.*

const val nameField = "name"
const val stanceField = "status"
const val stanceFieldSupporting = "1"

fun claimTypeCreate(ctx: Context) {
    ctx.html(
        Page {
            Head {
                title {
                    + gettext("Untitled")
                }
            }

            Body(ctx) {
                form {
                    method = FormMethod.post
                    action = "${Urls.ClaimTypes.create}"

                    label {
                        + gettext("name")

                        input {
                            type = InputType.text
                            name = nameField
                            autoComplete = false
                        }
                    }

                    div {
                        label {
                            input {
                                type = InputType.radio
                                name = stanceField
                                value = stanceFieldSupporting
                                required = true
                            }

                            + gettext("Supporting")
                        }

                        label {
                            input {
                                type = InputType.radio
                                name = stanceField
                                required = true
                            }

                            + gettext("Opposing")
                        }

                        br {}
                        br {}
                    }

                    button {
                        type = ButtonType.submit

                        + gettext("Submit")
                    }
                }
            }
        }
    )
}

fun claimTypeCreateHandler(ctx: Context) {
    val formName = ctx.formParam(nameField)
    val isSupporting = ctx.formParam(stanceField) == stanceFieldSupporting

    if (formName == null) {
        ctx.html(errorPage(ctx))
        return
    }

    DataLayer.ClaimTypes.create(
        ClaimType(UUID.randomUUID(), formName, isSupporting)
    )

    ctx.redirect("${Urls.ClaimTypes.view}")
}
