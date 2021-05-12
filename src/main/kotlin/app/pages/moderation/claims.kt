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
import app.pages.claims.ClaimAction
import app.pages.claims.ClaimsTable
import java.util.UUID

fun moderationActions(claim: Claim): FlowContent.() -> Unit {
    return {
        a {
            href = Urls.Moderation.claimApprove("${claim.id}").path

            + gettext("Approve")
        }

        + " "

        a {
            href = Urls.Moderation.claimReject("${claim.id}").path

            + gettext("Reject")
        }
    }
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

                ClaimsTable(claims, mapOf(), setOf(ClaimAction("Moderation", ::moderationActions)))()
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
