package app.pages

import io.javalin.http.Context
import kotlinx.html.h1
import kotlinx.html.title
import app.gettext

fun notFoundPage(ctx: Context) {
    ctx.html(
        Page {
            Head {
                title {
                    + gettext("404")
                }
            }
            Body(ctx) {
                h1 {
                    + gettext("Not found")
                }
            }
        }
    )
}
