package app.pages.users
import io.javalin.http.Context
import kotlinx.html.*
import app.DataLayer
import app.Helpers
import app.LOGIN_COOKIE_NAME
import app.SESSION_USER
import app.User
import app.gettext
import app.pages.Body
import app.pages.Head
import app.pages.Page
import app.pages.errorPage
import app.ui_components.FormGroup
import java.util.UUID
import javax.servlet.http.Cookie

const val usernameField = "username"
const val passwordField = "password"

fun renderLoginPage(ctx: Context, messages: List<String>? = null) {
    ctx.html(
        Page {
            Head {
                title {
                    + gettext("Login")
                }
            }

            Body(ctx) {
                if (messages != null) {
                    for (message in messages) {
                        p {
                            + message
                        }
                    }
                }

                form {
                    method = FormMethod.post
                    action = "${Urls.User.login}"

                    FormGroup(gettext("Username")) {
                        input {
                            type = InputType.text
                            name = usernameField
                            classes = setOf("form-control")
                        }
                    }

                    FormGroup(gettext("Password")) {
                        input {
                            type = InputType.password
                            name = passwordField
                            classes = setOf("form-control")
                        }
                    }

                    br

                    button {
                        type = ButtonType.submit
                        classes = setOf("btn btn-primary")

                        + gettext("Login")
                    }
                }
            }
        }
    )
}

fun loginPage(ctx: Context) {
    renderLoginPage(ctx)
}

fun doLogin(ctx: Context, userId: UUID) {
    val newToken = UUID.randomUUID()

    DataLayer.Users.updateLoginToken(userId, newToken)

    val cookie = Cookie(LOGIN_COOKIE_NAME, "$newToken")

    cookie.isHttpOnly = true
    cookie.maxAge = 60 * 60 * 24 * 30

    ctx.cookie(cookie)

    val user = DataLayer.Users.getById(userId)

    if (user != null) { // handles the edge case to show the user correctly at the same request as the user is registered
        ctx.attribute(SESSION_USER, user)
    }
}

fun loginHandler(ctx: Context) {
    val usernameValue = ctx.formParam(usernameField)
    val passwordValue = ctx.formParam(passwordField)

    if (passwordValue == null || usernameValue == null) {
        ctx.html(errorPage(ctx))
        return
    }

    val userId: UUID? = Helpers.parseUUID(usernameValue)
    val user: User? = if (userId == null) null else DataLayer.Users.getById(userId)

    if (userId != null && user != null && DataLayer.Users.isPasswordValid(user, passwordValue)) {
        doLogin(ctx, userId)

        ctx.redirect("${Helpers.getUrl("/")}")
    } else {
        renderLoginPage(ctx, listOf(gettext("Wrong password")))
    }
}

fun logoutHandler(ctx: Context) {
    Helpers.logout(ctx)
    ctx.redirect("${Helpers.getUrl("/")}")
}
