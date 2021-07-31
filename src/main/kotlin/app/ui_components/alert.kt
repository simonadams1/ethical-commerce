package app.ui_components

import kotlinx.html.FlowContent
import kotlinx.html.classes
import kotlinx.html.div

enum class ALERT_TYPE(val id: String) {
    PRIMARY("primary"),
    SUCCESS("success"),
    DANGER("danger"),
    INFO("info"),
    WARNING("warning"),
}

fun FlowContent.Alert(type: ALERT_TYPE = ALERT_TYPE.PRIMARY, timeout: Boolean = false, block: FlowContent.() -> Unit) {
    var _classes = setOf("alert", "alert-" + type.id)
    var _attributes = mapOf("role" to "alert")

    if (timeout) {
        _classes = _classes.plus("js-temp-message")
        _attributes = _attributes.plus("data-timeout" to "3000")
    }

    div {
        classes = _classes

        for ((key, value) in _attributes) {
            attributes[key] = value
        }

        block()
    }
}