package app.pages.parties.json_api

import io.javalin.http.Context
import app.DataLayer
import app.JsonApiSearchResult
import app.jQueryUiTermParam
import app.pages.errorPage

fun partySearch(ctx: Context) {
    val searchTerm = ctx.queryParam(jQueryUiTermParam)

    if (searchTerm == null) {
        ctx.html(errorPage(ctx))
        return
    }

    val result = DataLayer.Parties.query(searchTerm, 50).map({ JsonApiSearchResult("${it.id}", it.name) })

    ctx.json(result)
}
