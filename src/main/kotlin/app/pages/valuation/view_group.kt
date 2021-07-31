package app.pages.valuation

import app.*
import io.javalin.http.Context
import kotlinx.html.*
import app.pages.Body
import app.pages.Head
import app.pages.Page
import app.pages.errorPage
import app.ui_components.FlexBlockCenter
import java.util.*

val copyToMyValuationsField = "copy-"

fun viewGroup(ctx: Context) {
    val user = Helpers.getUserFromContext(ctx)
    val valuationGroupId = Helpers.parseUUID(ctx.pathParam(valuationGroupPlaceholder))

    if (user == null || valuationGroupId == null) {
        ctx.html(errorPage(ctx))
        return
    }

    val valuations = DataLayer.ValuationsG.getValuationsGU(user, valuationGroupId)
    val groupName = valuations.first().first.group.name

    val allChecked = true
    val bulkCheckboxesAllowed = valuations.any { it.second == null }

    ctx.html(
        Page {
            Head {
                title {
                    + gettext("My valuations")
                }
            }

            Body(ctx) {
                h1 {
                    + groupName
                }

                form {
                    method = FormMethod.post
                    action = Urls.Valuation.copyValuations(valuationGroupId.toString()).path

                    table {
                        classes = setOf("app-table", "js-table-bulk-checkboxes")

                        thead {
                            tr {
                                th {
                                    input {
                                        type = InputType.checkBox
                                        checked = if (bulkCheckboxesAllowed) allChecked else false
                                        disabled = !bulkCheckboxesAllowed
                                    }
                                }
                                th { + gettext("Cause") }
                                th { + gettext("State") }
                                th { + gettext("My valuation") }
                            }
                        }

                        tbody {
                            for ((valuationG, valuationU) in valuations) {
                                tr {
                                    td {
                                        val _disabled = valuationU != null
                                        input {
                                            type = InputType.checkBox
                                            checked = if (_disabled) false else allChecked
                                            disabled = _disabled

                                            if (!_disabled) { // allowed to copy
                                                name = copyToMyValuationsField + valuationG.id
                                            }
                                        }
                                    }
                                    td { + valuationG.cause.name }
                                    td {
                                        if (valuationG.isSupporting) {
                                            + gettext("Supporting")
                                        } else {
                                            + gettext("Opposing")
                                        }
                                    }
                                    td {
                                        if (valuationU != null) {
                                            if (valuationU.isSupporting) {
                                                + gettext("Supporting")
                                            } else {
                                                + gettext("Opposing")
                                            }
                                        }
                                    }
                                }
                            }

                            tr {
                                td {
                                    colSpan = "4"

                                    FlexBlockCenter {
                                        button {
                                            type = ButtonType.submit
                                            classes = setOf("btn", "btn-primary")

                                            + gettext("Copy to my valuations")
                                        }
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

fun handleCopyingValuations(ctx: Context) {
    val user = Helpers.getUserFromContext(ctx)

    if (user == null) {
        ctx.html(errorPage(ctx))
        return
    }

    val groupId = UUID.fromString(ctx.pathParam(valuationGroupPlaceholder))

    val valuationIdsToCopy = ctx.formParamMap()
        .filter { it.key.startsWith(copyToMyValuationsField) && it.value.first() == "on" }
        .map { UUID.fromString(it.key.replace(copyToMyValuationsField, "")) }
        .toSet()

    val valuationsToCopy = DataLayer.ValuationsG.getValuationsG(groupId).filter { valuationIdsToCopy.contains(it.id) }

    DataLayer.ValuationsU.create(user.id, valuationsToCopy)

    ctx.redirect(Urls.Valuation.singleGroup(groupId.toString()).path)
}
