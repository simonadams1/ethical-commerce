package app.pages.valuation

import app.*
import io.javalin.http.Context
import kotlinx.html.*
import app.pages.Body
import app.pages.Head
import app.pages.Page
import app.pages.errorPage
import app.ui_components.ALERT_TYPE
import app.ui_components.Alert

val causeFieldName = "cause"
val groupIdFieldName = "group"
val supportingFieldName = "status"
val supportingFieldValue = "1"
val actionTypeField = "actionType"

fun indexView(ctx: Context) {
    val user = Helpers.getUserFromContext(ctx)

    if (user == null) {
        ctx.html(errorPage(ctx))
        return
    }

    val valuations = DataLayer.ValuationsU.getUserValuations(user.id)

    ctx.html(
        Page {
            Head {
                title {
                    + gettext("My valuations")
                }
            }

            Body(ctx) {
                h1 {
                    + gettext("My valuations")
                }

                if (valuations.isEmpty()) {
                    Alert(ALERT_TYPE.INFO) {
                        + gettext("You have no valuations yet.")

                        + " "

                        a {
                            href = app.pages.causes.Urls.Causes.view.path

                            + gettext("Explore causes")
                        }

                        + " "

                        a {
                            href = Urls.Valuation.viewGroups.path

                            + gettext("Explore groups")
                        }
                    }
                } else {
                    table {
                        classes = setOf("app-table")

                        thead {
                            tr {
                                th { + gettext("Cause") }
                                th { + gettext("State") }
                            }
                        }

                        tbody {
                            for (valuation in valuations) {
                                tr {
                                    td { + valuation.cause.name }
                                    td {
                                        ValuationActions(
                                            valuation.cause.id,
                                            valuation,
                                            null,
                                            Urls.Valuation.index
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
