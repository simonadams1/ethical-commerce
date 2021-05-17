package app.pages.causes

import io.javalin.http.Context
import kotlinx.html.*
import app.DataLayer
import app.gettext
import app.pages.Body
import app.pages.Head
import app.pages.Page

fun causesView(ctx: Context) {
    val causes = DataLayer.Causes.getAll()

    ctx.html(
        Page {
            Head {
                title {
                    + gettext("Causes")
                }
            }

            Body(ctx) {
                h1 {
                    + gettext("Causes")
                }

                div {
                    a {
                        href = "${Urls.Causes.create}"

                        + gettext("Add new")
                    }

                    br {}
                    br {}
                }

                table {
                    classes = setOf("app-table")

                    tbody {
                        for (cause in causes) {
                            tr {
                                td { + cause.name }
                            }
                        }
                    }
                }
            }
        }
    )
}
