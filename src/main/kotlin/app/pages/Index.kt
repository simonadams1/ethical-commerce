package app.pages

import io.javalin.http.Context
import kotlinx.html.*
import app.gettext

fun indexPage(ctx: Context) {
    ctx.html(
        Page {
            Head {
                title {
                    + gettext("Untitled")
                }

                link {
                    rel = "stylesheet"
                    type = "text/css"
                    href = "/assets/style.css"
                }
            }

            Body(ctx) {

                div {
                    classes = setOf("wrapper")

                    + gettext("hello")
                }
            }
        }
    )
}
