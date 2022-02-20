package app.pages.claims

import io.javalin.Javalin
import io.javalin.core.security.SecurityUtil.roles
import app.Helpers
import app.Helpers.rolesAbove
import app.Navigation
import app.USER_ROLES
import app.WebPage
import app.gettext
import java.net.URL

const val claimIdPlaceholder = "claim-id"
const val partyIdQueryParam = "party-id"

object Urls {
    object Claims {
        val index = Helpers.getUrl("/")
        val add = Helpers.getUrl("claims/add")

        fun addSimilar(id: String): URL {
            return Helpers.getUrl("claims/add-similar/$id")
        }

        fun getEditPath(id: String): URL {
            return Helpers.getUrl("claims/$id/edit")
        }

        fun getDeletePath(id: String): URL {
            return Helpers.getUrl("claims/$id/delete")
        }

        fun singleClaim(id: String): URL {
            return Helpers.getUrl("claims/$id")
        }
    }
}

fun registerClaimsPages(app: Javalin) {
    Navigation.addPage(WebPage(gettext("Claims"), Urls.Claims.index))

    /*
    * /claims/add has a similar URL path as viewing a single claim via /claims/{id}
    * /claims/add route has to be registered first
    * */
    app.get(Urls.Claims.add.path, ::claimCreateForm, rolesAbove(USER_ROLES.MEMBER))
    app.post(Urls.Claims.add.path, ::claimCreateFormHandler, rolesAbove(USER_ROLES.MEMBER))

    app.get(Urls.Claims.addSimilar(":$claimIdPlaceholder").path, ::claimCreateSimilarForm, rolesAbove(USER_ROLES.MEMBER))

    app.get(Urls.Claims.index.path, ::viewClaims)
    app.get(Urls.Claims.singleClaim(":$claimIdPlaceholder").path, ::viewSingleClaim)

    app.get(Urls.Claims.getEditPath(":$claimIdPlaceholder").path, ::claimEditForm, roles(USER_ROLES.ADMINISTRATOR))
    app.get(Urls.Claims.getDeletePath(":$claimIdPlaceholder").path, ::claimDeleteHandler, roles(USER_ROLES.ADMINISTRATOR))
}
