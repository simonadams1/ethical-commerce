package app.pages.claim_types

import io.javalin.Javalin
import io.javalin.core.security.SecurityUtil.roles
import app.*

object Urls {
    object ClaimTypes {
        val view = Helpers.getUrl("claim-types")
        val create = Helpers.getUrl("claim-types/create")
    }
}

fun registerClaimTypePages(app: Javalin) {
    Navigation.addPage(WebPage(gettext("Claim types"), Urls.ClaimTypes.view, roles(USER_ROLES.ADMINISTRATOR)))

    app.get(Urls.ClaimTypes.view.path, ::claimTypesView, roles(USER_ROLES.ADMINISTRATOR))
    app.get(Urls.ClaimTypes.create.path, ::claimTypeCreate, roles(USER_ROLES.ADMINISTRATOR))
    app.post(Urls.ClaimTypes.create.path, ::claimTypeCreateHandler, roles(USER_ROLES.ADMINISTRATOR))
}
