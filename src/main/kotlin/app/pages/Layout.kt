package app.pages

import io.javalin.http.Context
import kotlinx.html.*
import kotlinx.html.stream.appendHTML

fun Page(block: HTML.() -> Unit): String {
    return buildString {
        appendln("<!DOCTYPE html>")
        appendHTML().html {
            block()
        }
    }
}

fun HTML.Head(block: HEAD.() -> Unit) {
    return head {
        meta {
            charset = "utf-8"
        }

        meta {
            name = "viewport"
            content = "width=device-width, initial-scale=1.0"
        }

        block()
    }
}

fun HTML.Body(ctx: Context, block: BODY.() -> Unit) {
    return body {
        div {
            classes = setOf("app-container")

            this@body.block()

            br // footer
        }
    }
}

fun errorPage(ctx: Context): String {
    return Page {
        Head {
            title {
                + "Error"
            }
        }
        Body(ctx) {
            h1 {
                + "Unknown error occurred."
            }
        }
    }
}
