package app.pages

import io.javalin.http.Context
import kotlinx.html.h1
import kotlinx.html.title

fun notFoundPage(ctx: Context) {
    ctx.html(
        Page {
            Head {
                title {
                    + "404"
                }
            }
            Body(ctx) {
                h1 {
                    + "Not found"
                }
            }
        }
    )
}
