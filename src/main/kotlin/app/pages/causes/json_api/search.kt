package app.pages.causes.json_api

import io.javalin.http.Context
import app.DataLayer
import app.Helpers
import app.JsonApiSearchResult
import app.jQueryUiTermParam
import app.pages.errorPage

fun causeSearch(ctx: Context) {
    val searchTerm = ctx.queryParam(jQueryUiTermParam)
    val user = Helpers.getUserFromContext(ctx)

    if (searchTerm == null || user == null) {
        ctx.html(errorPage(ctx))
        return
    }

    val result = DataLayer.Causes.query(user, searchTerm, 50).map({ (cause) -> JsonApiSearchResult("${cause.id}", cause.name) })

    ctx.json(result)
}
