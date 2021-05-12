package app.pages.causes

import io.javalin.Javalin
import io.javalin.core.security.SecurityUtil.roles
import app.*
import app.pages.causes.json_api.causeSearch

object Urls {
    object Causes {
        val view = Helpers.getUrl("causes")
        val create = Helpers.getUrl("causes/create")

        val search = Helpers.getUrl("causes/search")
    }
}

fun registerCausesPages(app: Javalin) {
    Navigation.addPage(WebPage(gettext("Causes"), Urls.Causes.view, roles(USER_ROLES.ADMINISTRATOR)))

    app.get(Urls.Causes.view.path, ::causesView, roles(USER_ROLES.ADMINISTRATOR))
    app.get(Urls.Causes.create.path, ::causeCreate, roles(USER_ROLES.ADMINISTRATOR))
    app.post(Urls.Causes.create.path, ::causeCreateHandler, roles(USER_ROLES.ADMINISTRATOR))

    app.get(Urls.Causes.search.path, ::causeSearch)
}
