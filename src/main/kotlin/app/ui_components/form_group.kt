package app.ui_components

import kotlinx.html.*

fun FlowContent.FormGroup(_label: String, block: FlowContent.() -> Unit) {
    div("mb-3") {
        label("form-label") {
            + _label
        }

        block()
    }
}

