package app.pages.causes.json_api

import io.javalin.http.Context
import app.DataLayer
import app.JsonApiSearchResult
import app.jQueryUiTermParam
import app.pages.errorPage

fun causeSearch(ctx: Context) {
    val searchTerm = ctx.queryParam(jQueryUiTermParam)

    if (searchTerm == null) {
        ctx.html(errorPage(ctx))
        return
    }

    val result = DataLayer.Causes.query(searchTerm, 50).map({ JsonApiSearchResult("${it.id}", it.name) })

    ctx.json(result)
}
