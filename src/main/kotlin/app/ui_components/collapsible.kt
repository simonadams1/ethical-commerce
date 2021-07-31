package app.ui_components

import app.Helpers.getId
import io.javalin.http.Context
import kotlinx.html.*

fun FlowContent.Collapsible(ctx: Context,label: String, block: FlowContent.() -> Unit) {
    val generated_id = getId(ctx)

    span("app--block") {
        button(classes = "btn btn-outline-primary btn-sm") {
            type = ButtonType.button
            attributes["data-bs-toggle"] = "collapse"
            attributes["data-bs-target"] = "#$generated_id"
            attributes["aria-expanded"] = "false"
            attributes["aria-controls"] = generated_id
            + label
        }
    }

    div("collapse") {
        id = generated_id

        br

        block()
    }
}

