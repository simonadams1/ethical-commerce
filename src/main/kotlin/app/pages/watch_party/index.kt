package app.pages.watch_party

import io.javalin.Javalin
import app.Helpers
import app.Helpers.rolesAbove
import app.Navigation
import app.USER_ROLES
import app.WebPage
import app.gettext

object Urls {
    object MonitorParties {
        val index = Helpers.getUrl("monitor-parties")
        val addEntry = Helpers.getUrl("monitor-parties/add")
    }
}

fun registerWatchParty(app: Javalin) {
    Navigation.addPage(WebPage(gettext("Monitor parties"), Urls.MonitorParties.index, rolesAbove(USER_ROLES.MEMBER)))

    app.get(Urls.MonitorParties.index.path, ::indexView, rolesAbove(USER_ROLES.MEMBER))
    app.post(Urls.MonitorParties.addEntry.path, ::handleAddEntry, rolesAbove(USER_ROLES.MEMBER))
}
