package app.pages.valuation

import io.javalin.http.Context
import kotlinx.html.*
import app.gettext
import app.pages.Body
import app.pages.Head
import app.pages.Page
import app.DataLayer
import app.Helpers
import app.pages.SelectFromRemote
import app.pages.errorPage
import java.util.UUID

val causeFieldName = "cause"
val groupIdFieldName = "group"
val supportingFieldName = "status"
val supportingFieldValue = "1"

fun indexView(ctx: Context) {
    val user = Helpers.getUserFromContext(ctx)

    if (user == null) {
        ctx.html(errorPage(ctx))
        return
    }

    val valuationByGroup = DataLayer.Valuations.getUserValuations(user.id).groupBy { it.group }
    val userValuations = DataLayer.Valuations.getUserValuations(user.id)

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

                for (grouping in valuationByGroup) {
                    val group = grouping.key
                    val valuations = grouping.value

                    h2 {
                        + group.name
                    }

                    form {
                        method = FormMethod.post
                        action = "${Urls.Valuation.addEntry}"

                        input {
                            type = InputType.hidden
                            name = groupIdFieldName
                            value = "${group.id}"
                        }

                        label {
                            + gettext("Cause")

                            SelectFromRemote(
                                app.pages.causes.Urls.Causes.search,
                                causeFieldName
                            )
                        }

                        label {
                            input {
                                type = InputType.radio
                                name = supportingFieldName
                                value = supportingFieldValue
                                required = true
                            }

                            + gettext("Supporting")
                        }

                        label {
                            input {
                                type = InputType.radio
                                name = supportingFieldName
                                required = true
                            }

                            + gettext("Opposing")
                        }

                        input {
                            type = InputType.submit
                            value = gettext("Add")
                        }
                    }

                    table {
                        classes = setOf("app-table")

                        thead {
                            tr {
                                th { + gettext("State") }
                                th { + gettext("Cause") }
                            }
                        }

                        tbody {
                            for (valuation in valuations) {
                                tr {
                                    td {
                                        if (valuation.isSupporting) {
                                            + gettext("Supporting")
                                        } else {
                                            + gettext("Opposing")
                                        }
                                    }

                                    td { + valuation.cause.name }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

fun handleAddEntry(ctx: Context) {
    val user = Helpers.getUserFromContext(ctx)
    val causeId = UUID.fromString(ctx.formParam(causeFieldName))
    val groupId = UUID.fromString(ctx.formParam(groupIdFieldName))

    val isPositive = ctx.formParam(supportingFieldName) == supportingFieldValue

    if (user == null) {
        ctx.html(errorPage(ctx))
        return
    }

    DataLayer.Valuations.create(groupId, causeId, isPositive)

    ctx.redirect("${Urls.Valuation.index}")
}
