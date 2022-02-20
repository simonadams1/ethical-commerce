package app.ui_components

import app.Helpers
import io.javalin.http.Context
import kotlinx.html.FlowContent
import kotlinx.html.*
import java.net.URL

enum class DROPDOWN_STYLE(val id: String) {
    PRIMARY("primary"),
    SUCCESS("success"),
    DANGER("danger"),
    INFO("info"),
    WARNING("warning"),
}

data class DropdownOption(
    val label: String,
    val href: URL
)

fun FlowContent.Dropdown(
    ctx: Context,
    label: String,
    options: Iterable<DropdownOption>,
    style: DROPDOWN_STYLE = DROPDOWN_STYLE.PRIMARY
) {
    val dropdownId = "dropdown-${Helpers.getId(ctx)}"

    div("dropdown") {
        a(classes = "btn btn-${style.id} dropdown-toggle") {
            href = "#"
            role = "button"
            id = dropdownId
            attributes["data-bs-toggle"] = "dropdown"
            attributes["aria-expanded"] = "false"
            + label
        }

        ul("dropdown-menu") {
            attributes["aria-labelledby"] = dropdownId

            for (option in options) {
                li {
                    a(classes = "dropdown-item") {
                        href = "${option.href}"
                        + option.label
                    }
                }
            }
        }
    }
}

fun FlowContent.DropdownIconOnly(
    ctx: Context,
    label: String,
    icon: String,
    options: Iterable<DropdownOption>
) {
    val dropdownId = "dropdown-${Helpers.getId(ctx)}"

    div("dropdown") {
        button {
            type = ButtonType.button
            id = dropdownId
            attributes["data-bs-toggle"] = "dropdown"
            attributes["aria-expanded"] = "false"
            attributes["aria-label"] = label

            i("bi bi-$icon")
        }

        ul("dropdown-menu") {
            attributes["aria-labelledby"] = dropdownId

            for (option in options) {
                li {
                    a(classes = "dropdown-item") {
                        href = "${option.href}"
                        + option.label
                    }
                }
            }
        }
    }
}
