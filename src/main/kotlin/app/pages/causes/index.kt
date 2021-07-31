package app.pages.causes

import io.javalin.Javalin
import app.*
import app.Helpers.rolesAbove
import app.pages.causes.json_api.causeSearch

object Urls {
    object Causes {
        val view = Helpers.getUrl("causes")
        val create = Helpers.getUrl("causes/create")

        val search = Helpers.getUrl("causes/search")
    }
}

fun registerCausesPages(app: Javalin) {
    Navigation.addPage(WebPage(gettext("Causes"), Urls.Causes.view, rolesAbove(USER_ROLES.MEMBER)))

    app.get(Urls.Causes.view.path, ::causesView, rolesAbove(USER_ROLES.MEMBER))
    app.get(Urls.Causes.create.path, ::causeCreate, rolesAbove(USER_ROLES.ADMINISTRATOR))
    app.post(Urls.Causes.create.path, ::causeCreateHandler, rolesAbove(USER_ROLES.ADMINISTRATOR))

    app.get(Urls.Causes.search.path, ::causeSearch)
}
