package app.pages.valuation

import app.*
import app.pages.errorPage
import app.ui_components.FlexBlock
import io.javalin.http.Context
import kotlinx.html.*
import java.net.URL
import java.util.*

val valuationActionsRedirectField = "val-actions-redirect"
val valuationActionsValuationId = "val-id"

enum class VALUATION_ACTIONS(val id: String) {
    SUPPORT("0"),
    OPPOSE("1"),
    REMOVE_VALUATION("2");

    companion object {
        fun fromId(id: String) = values().first { it.id == id }
    }
}

fun FlowContent.ValuationActions(
    causeId: UUID,
    valuation: ValuationU?,
    group: ValuationGroup?,
    redirectTo: URL
) {
    fun formTemplate(block: FlowContent.() -> Unit) {
        form {
            method = FormMethod.post
            action = Urls.Valuation.valuationUAction.path

            if (group != null) {
                input {
                    type = InputType.hidden
                    name = groupIdFieldName
                    value = "${group.id}"
                }
            }

            input {
                type = InputType.hidden
                name = causeFieldName
                value = "$causeId"
            }

            if (valuation != null) {
                input {
                    type = InputType.hidden
                    name = valuationActionsValuationId
                    value = "${valuation.id}"
                }
            }

            input {
                type = InputType.hidden
                name = valuationActionsRedirectField
                value = "$redirectTo"
            }

            block()
        }
    }

    FlexBlock {
        for (option in VALUATION_ACTIONS.values()) {
            when (option) {
                VALUATION_ACTIONS.SUPPORT ->
                    formTemplate {
                        input {
                            type = InputType.hidden
                            name = actionTypeField
                            value = VALUATION_ACTIONS.SUPPORT.id
                        }

                        val _disabled = valuation?.isSupporting == true

                        button {
                            type = ButtonType.submit
                            disabled = _disabled
                            classes = setOf("btn", "btn-sm", if (_disabled) "btn-outline-secondary" else "btn-outline-primary")

                            + gettext("Support")
                        }

                        + " "
                    }
                VALUATION_ACTIONS.OPPOSE ->
                    formTemplate {
                        input {
                            type = InputType.hidden
                            name = actionTypeField
                            value = VALUATION_ACTIONS.OPPOSE.id
                        }

                        val _disabled = valuation?.isSupporting == false

                        button {
                            type = ButtonType.submit
                            disabled = _disabled
                            classes = setOf("btn", "btn-sm", if (_disabled) "btn-outline-secondary" else "btn-outline-primary")

                            + gettext("Oppose")
                        }

                        + " "
                    }
                VALUATION_ACTIONS.REMOVE_VALUATION ->
                    formTemplate {
                        input {
                            type = InputType.hidden
                            name = actionTypeField
                            value = VALUATION_ACTIONS.REMOVE_VALUATION.id
                        }

                        val _disabled = valuation == null

                        button {
                            type = ButtonType.submit
                            disabled = _disabled
                            classes = setOf("btn", "btn-sm", if (_disabled) "btn-outline-secondary" else "btn-outline-primary")

                            + gettext("Neutral")
                        }
                    }
            }
        }
    }
}

fun handleValuationAction(ctx: Context) {
    val user = Helpers.getUserFromContext(ctx)
    val causeId = Helpers.parseUUID(ctx.formParam(causeFieldName))
    val actionTypeStr = ctx.formParam(actionTypeField)
    val redirectTo = ctx.formParam(valuationActionsRedirectField)
    val valuationId = Helpers.parseUUID(ctx.formParam(valuationActionsValuationId))


    if (user == null || causeId == null || actionTypeStr == null || redirectTo == null) {
        ctx.html(errorPage(ctx))
        return
    }

    when (VALUATION_ACTIONS.fromId(actionTypeStr)) {
        VALUATION_ACTIONS.SUPPORT -> {
            if (valuationId == null) {
                DataLayer.ValuationsU.create(user.id, causeId, true, null)
            } else {
                DataLayer.ValuationsU.update(valuationId, true)
            }
        }

        VALUATION_ACTIONS.OPPOSE -> {
            if (valuationId == null) {
                DataLayer.ValuationsU.create(user.id, causeId, false, null)
            } else {
                DataLayer.ValuationsU.update(valuationId, false)
            }
        }

        VALUATION_ACTIONS.REMOVE_VALUATION -> {
            if (valuationId != null) {
                DataLayer.ValuationsU.delete(valuationId)
            }
        }
    }

    ctx.redirect(redirectTo)
}