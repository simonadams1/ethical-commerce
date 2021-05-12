package app.pages.parties

import io.javalin.http.Context
import kotlinx.html.*
import app.*
import app.pages.Body
import app.pages.Head
import app.pages.Page
import app.pages.errorPage
import java.util.*

val nameField = "name"

fun partyCreate(ctx: Context) {
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
                    action = "${Urls.Parties.create}"

                    input {
                        type = InputType.text
                        name = nameField
                        autoComplete = false
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

fun partyCreateHandler(ctx: Context) {
    val formName = ctx.formParam(app.pages.claim_types.nameField)

    if (formName == null) {
        ctx.html(errorPage(ctx))
        return
    }

    DataLayer.Parties.create(
        Party(UUID.randomUUID(), formName)
    )

    ctx.redirect("${Urls.Parties.view}")
}
