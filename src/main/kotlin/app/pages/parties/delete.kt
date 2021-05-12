package app.pages.parties

import io.javalin.http.Context
import app.*
import app.pages.errorPage
import java.util.*

val idField = "id"

fun partyDeleteHandler(ctx: Context) {
    val id = ctx.formParam(idField)

    if (id == null) {
        ctx.html(errorPage(ctx))
        return
    }

    DataLayer.Parties.delete(UUID.fromString(id))

    ctx.redirect("${Urls.Parties.view}")
}
