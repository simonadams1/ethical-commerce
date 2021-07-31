package app.ui_components

import kotlinx.html.*
import java.net.URL

enum class BUTTON_TYPE(val id: String) {
    PRIMARY("primary"),
    SUCCESS("success"),
    DANGER("danger"),
    INFO("warning"),
    WARNING("warning"),
}

fun FlowOrInteractiveOrPhrasingContent.LinkButton(label: String, url: URL, type: BUTTON_TYPE = BUTTON_TYPE.PRIMARY) {
    a {
        href = "$url"
        classes = setOf("btn", "btn-" + type.id)

        + label
    }
}

fun FlowOrInteractiveOrPhrasingContent.LinkButton(label: String, url: URL, type: BUTTON_TYPE = BUTTON_TYPE.PRIMARY, attrs: Map<String, String>) {
    a {
        href = "$url"
        classes = setOf("btn", "btn-" + type.id)

        for ((key, value) in attrs) {
            attributes[key] = value
        }

        + label
    }
}

