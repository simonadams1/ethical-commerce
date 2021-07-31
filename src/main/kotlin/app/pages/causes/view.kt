package app.pages.causes

import app.*
import io.javalin.http.Context
import kotlinx.html.*
import app.pages.Body
import app.pages.Head
import app.pages.Page
import app.pages.errorPage
import app.pages.valuation.ValuationActions

fun causesView(ctx: Context) {
    val user = Helpers.getUserFromContext(ctx)

    if (user == null) {
        ctx.html(errorPage(ctx))
        return
    }

    val pagination = PaginationInfo(
        DataLayer.Causes.getCount(),
        ctx.queryParam("page")?.toInt() ?: 1
    )

    val causes = DataLayer.Causes.queryUserCauses(user, pagination.offset, pagination.pageSize)

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

                    thead {
                        tr {
                            th { + gettext("Cause") }
                            th { + gettext("Actions") }
                        }
                    }

                    tbody {
                        for ((cause, valuationU) in causes) {
                            tr {
                                td { + cause.name }
                                td {
                                    ValuationActions( cause.id, valuationU, null, Urls.Causes.view)
                                }
                            }
                        }
                    }
                }

                br

                Pagination(ctx, pagination)
            }
        }
    )
}
