package app.ui_components

import kotlinx.html.*

fun FlowContent.FlexBlock(block: FlowContent.() -> Unit) {
    span("app--flex-block") {
        block()
    }
}

fun FlowContent.FlexBlockCenter(block: FlowContent.() -> Unit) {
    span("app--flex-block-center") {
        block()
    }
}
