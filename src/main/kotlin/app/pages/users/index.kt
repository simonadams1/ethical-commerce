package app.pages.users

import io.javalin.Javalin
import app.*

object Urls {
    object User {
        val register = Helpers.getUrl("register")
        val login = Helpers.getUrl("login")
        val logout = Helpers.getUrl("logout")
        val account = Helpers.getUrl("account")
        val notifications = Helpers.getUrl("account/notifications")
        val notificationRemove = Helpers.getUrl("account/notifications/remove")
    }
}

fun registerUsersPage(app: Javalin) {
    app.get(Urls.User.register.path, ::registerHandler)

    app.get(Urls.User.login.path, ::loginPage)
    app.post(Urls.User.login.path, ::loginHandler)

    app.get(Urls.User.logout.path, ::logoutHandler, Helpers.rolesAbove(USER_ROLES.MEMBER))

    app.get(Urls.User.account.path, ::accountPage, Helpers.rolesAbove(USER_ROLES.MEMBER))

    app.get(Urls.User.notifications.path, ::viewNotifications, Helpers.rolesAbove(USER_ROLES.MEMBER))
    app.post(Urls.User.notificationRemove.path, ::handleNotificationRemove, Helpers.rolesAbove(USER_ROLES.MEMBER))
}
