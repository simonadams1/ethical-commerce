package app.pages.causes

import io.javalin.http.Context
import kotlinx.html.*
import app.Cause
import app.DataLayer
import app.gettext
import app.pages.Body
import app.pages.Head
import app.pages.Page
import app.pages.errorPage
import java.util.*

const val nameField = "name"

fun causeCreate(ctx: Context) {
    ctx.html(
        Page {
            Head {
                title {
                    + gettext("Create a cause")
                }
            }

            Body(ctx) {
                form {
                    method = FormMethod.post
                    action = "${Urls.Causes.create}"

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

fun causeCreateHandler(ctx: Context) {
    val formName = ctx.formParam(nameField)

    if (formName == null) {
        ctx.html(errorPage(ctx))
        return
    }

    DataLayer.Causes.create(
        Cause(UUID.randomUUID(), formName, null)
    )

    ctx.redirect("${Urls.Causes.view}")
}
