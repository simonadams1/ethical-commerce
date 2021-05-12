package app.pages.users

import io.javalin.http.Context
import kotlinx.html.*
import kotlinx.html.title
import app.gettext
import app.pages.Body
import app.pages.Head
import app.pages.Page
import app.DataLayer
import app.Helpers
import app.pages.errorPage
import java.util.UUID

val notificationField = "notification"

fun viewNotifications(ctx: Context) {
    val currentUser = Helpers.getUserFromContext(ctx)

    if (currentUser == null) {
        ctx.html(errorPage(ctx))
        return
    }

    val notifications = DataLayer.UserNotifications.getAll(currentUser.id)

    ctx.html(
        Page {
            Head {
                title {
                    + gettext("Notifications")
                }
            }

            Body(ctx) {
                if (notifications.isEmpty()) {
                    p {
                        + gettext("You have no notifications")
                    }
                } else {
                    h2 {
                        + gettext("Notifications")
                    }

                    ul {
                        for (notification in notifications) {
                            li {
                                + notification.message

                                form {
                                    method = FormMethod.post
                                    action = "${Urls.User.notificationRemove}"

                                    input {
                                        type = InputType.hidden
                                        name = notificationField
                                        value = "${notification.id}"
                                    }

                                    input {
                                        type = InputType.submit
                                        value = "X"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

fun handleNotificationRemove(ctx: Context) {
    val currentUser = Helpers.getUserFromContext(ctx)

    if (currentUser == null) {
        ctx.html(errorPage(ctx))
        return
    }

    val notificationId = UUID.fromString(ctx.formParam(notificationField))

    DataLayer.UserNotifications.delete(currentUser.id, notificationId)

    ctx.redirect("${Urls.User.notifications}")
}
