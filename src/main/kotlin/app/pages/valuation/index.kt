package app.pages.valuation

import io.javalin.Javalin
import app.Helpers
import app.Helpers.rolesAbove
import app.Navigation
import app.USER_ROLES
import app.WebPage
import app.gettext
import app.pages.valuation.json_api.groupSearch

object Urls {
    object Valuation {
        val searchGroups = Helpers.getUrl("valuation/groups/search")
        val index = Helpers.getUrl("valuation")
        val addEntry = Helpers.getUrl("valuation/add")
        val valuationGroupAdd = Helpers.getUrl("valuation-group/add")
        val valuationGroupChangeMemberShipStatus = Helpers.getUrl("valuation-group/alter-membership")
    }
}

fun registerValuation(app: Javalin) {
    Navigation.addPage(WebPage(gettext("Valuation"), Urls.Valuation.index, rolesAbove(USER_ROLES.MEMBER)))
    Navigation.addPage(WebPage(gettext("Valuation groups"), Urls.Valuation.valuationGroupAdd, rolesAbove(USER_ROLES.MEMBER)))

    app.get(Urls.Valuation.searchGroups.path, ::groupSearch, rolesAbove(USER_ROLES.MEMBER))

    app.get(Urls.Valuation.index.path, ::indexView, rolesAbove(USER_ROLES.MEMBER))
    app.post(Urls.Valuation.addEntry.path, ::handleAddEntry, rolesAbove(USER_ROLES.MEMBER))

    app.get(Urls.Valuation.valuationGroupAdd.path, ::valuationGroupsView, rolesAbove(USER_ROLES.MEMBER))
    app.post(Urls.Valuation.valuationGroupAdd.path, ::handleAddValuationGroup, rolesAbove(USER_ROLES.MEMBER))

    app.post(Urls.Valuation.valuationGroupChangeMemberShipStatus.path, ::handleMembershipStatusChange, rolesAbove(USER_ROLES.MEMBER))
}
