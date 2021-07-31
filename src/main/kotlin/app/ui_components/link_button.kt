package app.ui_components

import kotlinx.html.*
import java.net.URL

enum class BUTTON_STYLE(val id: String) {
    PRIMARY("primary"),
    SUCCESS("success"),
    DANGER("danger"),
    INFO("warning"),
    WARNING("warning"),
}

fun FlowOrInteractiveOrPhrasingContent.LinkButton(label: String, url: URL, style: BUTTON_STYLE = BUTTON_STYLE.PRIMARY) {
    a {
        href = "$url"
        classes = setOf("btn", "btn-" + style.id)

        + label
    }
}

fun FlowOrInteractiveOrPhrasingContent.LinkButton(label: String, url: URL, style: BUTTON_STYLE = BUTTON_STYLE.PRIMARY, attrs: Map<String, String>) {
    a {
        href = "$url"
        classes = setOf("btn", "btn-" + style.id)

        for ((key, value) in attrs) {
            attributes[key] = value
        }

        + label
    }
}

