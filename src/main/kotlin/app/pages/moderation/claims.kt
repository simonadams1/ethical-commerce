package app.pages.moderation

import io.javalin.http.Context
import app.gettext
import app.pages.Body
import app.pages.Head
import app.pages.Page
import app.DataLayer
import kotlinx.html.*
import app.Claim
import app.MODERATION_STATUS
import app.pages.claims.ClaimsTable
import app.ui_components.DropdownOption
import java.util.UUID

fun moderationActions(claim: Claim): List<DropdownOption> {
    return listOf(
        DropdownOption(gettext("Approve"), Urls.Moderation.claimApprove("${claim.id}")),
        DropdownOption(gettext("Reject"), Urls.Moderation.claimReject("${claim.id}"))
    )
}

fun viewClaimsQueue(ctx: Context) {
    val claims = DataLayer.Claims.getForModeration()

    ctx.html(
        Page {
            Head {
                title {
                    + gettext("Moderation queue")
                }
            }

            Body(ctx) {
                h1 {
                    + gettext("Moderation queue")
                }

                ClaimsTable(ctx, claims, mapOf(), ::moderationActions)()
            }
        }
    )
}

fun handleApprove(ctx: Context) {
    val claimId = UUID.fromString(ctx.pathParam(moderationClaimIdPlaceholder))

    DataLayer.Claims.setModerationStatus(claimId, MODERATION_STATUS.APPROVED)
    DataLayer.UserNotifications.create(claimId)

    ctx.redirect("${Urls.Moderation.claims}")
}

fun handleReject(ctx: Context) {
    val id = UUID.fromString(ctx.pathParam(moderationClaimIdPlaceholder))

    DataLayer.Claims.setModerationStatus(id, MODERATION_STATUS.REJECTED)

    ctx.redirect("${Urls.Moderation.claims}")
}
