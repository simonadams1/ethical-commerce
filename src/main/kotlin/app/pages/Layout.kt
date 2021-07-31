package app.pages

import app.*
import app.Helpers.rolesAbove
import java.net.URL
import io.javalin.http.Context
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import app.pages.users.Urls

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

        link {
            rel = "stylesheet"
            href = "${Helpers.getUrl("assets/jquery-ui-1.12.1.custom/jquery-ui.css")}"
        }

        link {
            rel = "stylesheet"
            href = "${Helpers.getUrl("assets/jquery-ui-1.12.1.custom/jquery-ui.theme.css")}"
        }

        link {
            rel = "stylesheet"
            href = "https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css"
        }

        link {
            rel = "stylesheet"
            href = "${Helpers.getUrl("assets/app.css")}"
        }

        block()
    }
}

fun HTML.Body(ctx: Context, block: BODY.() -> Unit) {
    return body {
        div {
            classes = setOf("app-container")

            NavigationMenu(ctx)

            this@body.block()

            script {
                type = "text/javascript"
                src = "${Helpers.getUrl("assets/jquery-ui-1.12.1.custom/external/jquery/jquery.js")}"
            }

            script {
                type = "text/javascript"
                src = "${Helpers.getUrl("assets/jquery-ui-1.12.1.custom/jquery-ui.js")}"
            }

            script {
                type = "text/javascript"
                src = "https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js"
            }

            script {
                type = "text/javascript"
                src = "${Helpers.getUrl("assets/app.js")}"
            }
        }
    }
}

fun DIV.NavigationMenu(ctx: Context) {
    val user = Helpers.getUserFromContext(ctx)
    val mainPages = Navigation.pages.filter { it.roles.find { role -> rolesAbove(USER_ROLES.MODERATOR).contains(role) } == null }

    val moderationPages = Navigation.pages
        .filter { it.roles.find { role -> rolesAbove(USER_ROLES.MODERATOR).contains(role) } != null }
        .filter { page -> user != null && page.roles.contains(user.role) }

    if (moderationPages.isNotEmpty()) {
        div {
            classes = setOf("app--space-between", "app--header")

            ul {
                classes = setOf("app--navigation")

                li { + gettext("Moderation tools") }

                for (page in moderationPages) {
                    li {
                        a {
                            href = "${page.url}"

                            + page.name
                        }
                    }
                }
            }
        }
    }

    div {
        classes = setOf("app--space-between", "app--header")

        ul {
            classes = setOf("app--navigation")

            for (page in mainPages) {
                if (page.roles.isEmpty() || (user != null && page.roles.contains(user.role))) {
                    li {
                        a {
                            href = "${page.url}"

                            + page.name
                        }
                    }
                }
            }
        }

        if (user != null) {
            div {
                if (user.notificationsCount > 0) {
                    a {
                        href = Urls.User.notifications.path

                        + "${gettext("Notifications")} (${user.notificationsCount})"
                    }

                    + " / "
                }
                a {
                    href = Urls.User.account.path

                    + gettext("My account")
                }

                + " / "

                a {
                    href = Urls.User.logout.path

                    + gettext("Logout")
                }
            }
        } else {
            div {
                a {
                    href = Urls.User.login.path

                    + gettext("Login")
                }

                + " / "

                a {
                    href = Urls.User.register.path

                    + gettext("Register")
                }
            }
        }
    }
}

fun FlowContent.SelectFromRemote(url: URL, inputName: String, predefinedValue: JsonApiSearchResult? = null) {
    input {
        classes = setOf("app--select-from-remote")
        attributes["data-search-url"] = "$url"

        type = InputType.text
        value = predefinedValue?.label ?: ""
        autoComplete = false
    }

    input { // JS implementation depends on this being the next sibling of .app--select-from-remote
        classes = setOf("app--select-from-remote--hidden")
        type = InputType.hidden
        name = inputName
        value = predefinedValue?.id ?: ""
    }
}

fun FlowContent.AutocompleteFromRemote(url: URL, inputName: String, predefinedValue: JsonApiSearchResult? = null) {
    input {
        classes = setOf("app--autocomplete-from-remote")
        attributes["data-search-url"] = "$url"
        name = inputName
        type = InputType.text
        value = predefinedValue?.label ?: ""
        autoComplete = false
    }
}

fun FlowContent.TemporaryMessage(message: String, timeout: Number = 3000) {
    div {
        classes = setOf("js-temp-message")

        attributes["data-timeout"] = "$timeout"

        + message
    }
}

fun errorPage(ctx: Context): String {
    return Page {
        Head {
            title {
                + gettext("Error")
            }
        }
        Body(ctx) {
            h1 {
                + gettext("Unknown error occurred.")
            }
        }
    }
}
