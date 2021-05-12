package app

import io.javalin.http.Context
import kotlinx.html.*
import app.pages.Body
import app.pages.Head
import app.pages.Page

fun testPage(ctx: Context) {
    ctx.html(
        Page {
            Head {
                title {
                    + gettext("Test page")
                }
            }

            Body(ctx) {
                div {
                    classes = setOf("accordion")

                    h3 {
                        + "Section 1"
                    }

                    div {
                        p {
                            + "Mauris mauris ante, blandit et, ultrices a, suscipit eget, quam. Integer "
                        }
                    }

                    h3 {
                        + "Section 2"
                    }

                    div {
                        p {
                            + "Mauris mauris ante, blandit et, ultrices a, suscipit eget, quam. Integer "
                        }
                    }
                }
            }
        }
    )
}
