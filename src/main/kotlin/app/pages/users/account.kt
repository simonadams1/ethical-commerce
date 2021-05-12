package app.pages.users

import io.javalin.http.Context
import kotlinx.html.*
import app.gettext
import app.pages.Body
import app.pages.Head
import app.pages.Page

fun accountPage(ctx: Context) {
    ctx.html(
        Page {
            Head {
                title {
                    + gettext("My account")
                }
            }

            Body(ctx) {
                // TODO: change password, delete account
            }
        }
    )
}
