package app.pages.parties

import io.javalin.Javalin
import io.javalin.core.security.SecurityUtil.roles
import app.Helpers
import app.Navigation
import app.USER_ROLES
import app.WebPage
import app.gettext
import app.pages.parties.json_api.partySearch

object Urls {
    object Parties {
        val create = Helpers.getUrl("parties/create")
        val view = Helpers.getUrl("parties/view")
        val delete = Helpers.getUrl("parties/delete")

        val search = Helpers.getUrl("parties/search")
    }
}

fun registerPartiesPages(app: Javalin) {
    Navigation.addPage(WebPage(gettext("Parties"), Urls.Parties.view, roles(USER_ROLES.ADMINISTRATOR)))

    app.get(Urls.Parties.view.path, ::viewParties, roles(USER_ROLES.ADMINISTRATOR))
    app.get(Urls.Parties.create.path, ::partyCreate, roles(USER_ROLES.ADMINISTRATOR))
    app.post(Urls.Parties.create.path, ::partyCreateHandler, roles(USER_ROLES.ADMINISTRATOR))
    app.post(Urls.Parties.delete.path, ::partyDeleteHandler, roles(USER_ROLES.ADMINISTRATOR))

    app.get(Urls.Parties.search.path, ::partySearch)
}
