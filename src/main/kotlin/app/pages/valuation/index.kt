package app.pages.valuation

import io.javalin.Javalin
import app.Helpers
import app.Helpers.rolesAbove
import app.Navigation
import app.USER_ROLES
import app.WebPage
import app.gettext
import app.pages.valuation.json_api.groupSearch
import java.net.URL

val valuationGroupPlaceholder = "valuation-group-id"
val valuationGPlaceholder = "valuation-g-id"

object Urls {
    object Valuation {
        val searchGroups = Helpers.getUrl("valuation/groups/search")
        val index = Helpers.getUrl("valuation")
        val viewGroups = Helpers.getUrl("valuation-groups")
        val valuationGroupAdd = Helpers.getUrl("valuation-group/add")
        val valuationGroupChangeMemberShipStatus = Helpers.getUrl("valuation-group/alter-membership")
        val valuationUAction = Helpers.getUrl("valuations/action")

        fun singleGroup(id: String): URL {
            return Helpers.getUrl("valuation-groups/$id")
        }

        fun deleteGroup(id: String): URL {
            return Helpers.getUrl("valuation-groups/$id/delete")
        }

        fun deleteValuationG(group_id: String, valuation_id: String): URL {
            return Helpers.getUrl("valuation-groups/$group_id/delete/$valuation_id")
        }

        fun addValuationToGroup(id: String): URL {
            return Helpers.getUrl("valuation-groups/$id/add-item")
        }

        fun copyValuations(id: String): URL {
            return Helpers.getUrl("valuation-groups/$id/copy")
        }
    }
}

fun registerValuation(app: Javalin) {
    Navigation.addPage(WebPage(gettext("Valuations"), Urls.Valuation.index, rolesAbove(USER_ROLES.MEMBER)))
    Navigation.addPage(WebPage(gettext("Valuation groups"), Urls.Valuation.viewGroups, rolesAbove(USER_ROLES.MEMBER)))

    app.get(Urls.Valuation.searchGroups.path, ::groupSearch, rolesAbove(USER_ROLES.MEMBER))

    app.get(Urls.Valuation.index.path, ::indexView, rolesAbove(USER_ROLES.MEMBER))
    app.get(Urls.Valuation.singleGroup(":$valuationGroupPlaceholder").path, ::viewGroup, rolesAbove(USER_ROLES.MEMBER))

    app.get(Urls.Valuation.viewGroups.path, ::valuationGroupsView, rolesAbove(USER_ROLES.MEMBER))

    app.get(Urls.Valuation.valuationGroupAdd.path, ::addGroupPage, rolesAbove(USER_ROLES.MEMBER))
    app.post(Urls.Valuation.valuationGroupAdd.path, ::handleAddValuationGroup, rolesAbove(USER_ROLES.MEMBER))

    app.post(Urls.Valuation.valuationUAction.path, ::handleValuationAction, rolesAbove(USER_ROLES.MEMBER))

    app.post(Urls.Valuation.valuationGroupChangeMemberShipStatus.path, ::handleMembershipStatusChange, rolesAbove(USER_ROLES.MEMBER))
    app.post(Urls.Valuation.copyValuations(":$valuationGroupPlaceholder").path, ::handleCopyingValuations, rolesAbove(USER_ROLES.MEMBER))

    app.get(Urls.Valuation.addValuationToGroup(":$valuationGroupPlaceholder").path, ::addValuationItemToGroupPage, rolesAbove(USER_ROLES.MEMBER))
    app.post(Urls.Valuation.addValuationToGroup(":$valuationGroupPlaceholder").path, ::handleAddValuationItemToGroup, rolesAbove(USER_ROLES.MEMBER))

    app.get(Urls.Valuation.deleteValuationG(":$valuationGroupPlaceholder", ":$valuationGPlaceholder").path, ::handleRemoveValuationItemFromGroup, rolesAbove(USER_ROLES.MEMBER))

    app.post(Urls.Valuation.deleteGroup(":$valuationGroupPlaceholder").path, ::handleDeleteGroup, rolesAbove(USER_ROLES.MEMBER))
}
