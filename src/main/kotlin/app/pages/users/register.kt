package app.pages.users

import io.javalin.http.Context
import kotlinx.html.*
import app.DataLayer
import app.Helpers
import app.gettext
import app.pages.Body
import app.pages.Head
import app.pages.Page
import java.util.UUID

fun registerHandler(ctx: Context) {
    Helpers.logout(ctx)

    val result = DataLayer.Users.create()

    doLogin(ctx, UUID.fromString(result.username))

    ctx.html(
        Page {
            Head {
                title {
                    + gettext("Registration successful")
                }
            }

            Body(ctx) {
                h1 {
                    + gettext("Registration successful")
                }

                dl {
                    dt { + gettext("username") }
                    dd { + result.username }

                    dt { + gettext("password") }
                    dd { + result.passwordPlainText }
                }
            }
        }
    )
}
