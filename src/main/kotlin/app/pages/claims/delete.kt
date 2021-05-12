package app.pages.claims

import io.javalin.http.Context
import java.util.UUID
import app.DataLayer

fun claimDeleteHandler(ctx: Context) {
    val id = ctx.pathParam(claimIdPlaceholder)

    DataLayer.Claims.delete(UUID.fromString(id))

    ctx.redirect("${Urls.Claims.index}")
}
