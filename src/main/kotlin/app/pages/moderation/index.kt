package app.pages.moderation

import io.javalin.Javalin
import app.Helpers
import app.Helpers.rolesAbove
import app.Navigation
import app.USER_ROLES
import app.WebPage
import app.gettext
import java.net.URL

object Urls {
    object Moderation {
        val claims = Helpers.getUrl("moderation/claims")

        fun claimApprove(id: String): URL {
            return Helpers.getUrl("moderation/claims/$id/approve")
        }

        fun claimReject(id: String): URL {
            return Helpers.getUrl("moderation/claims/$id/reject")
        }
    }
}

const val moderationClaimIdPlaceholder = "moderation-claim-id"

fun registerModeration(app: Javalin) {
    Navigation.addPage(WebPage(gettext("Moderation"), Urls.Moderation.claims, rolesAbove(USER_ROLES.MODERATOR)))

    app.get(Urls.Moderation.claims.path, ::viewClaimsQueue, rolesAbove(USER_ROLES.MODERATOR))
    app.get(Urls.Moderation.claimApprove(":$moderationClaimIdPlaceholder").path, ::handleApprove, rolesAbove(USER_ROLES.MODERATOR))
    app.get(Urls.Moderation.claimReject(":$moderationClaimIdPlaceholder").path, ::handleReject, rolesAbove(USER_ROLES.MODERATOR))
}
