package app.pages.valuation.json_api

import io.javalin.http.Context
import app.DataLayer
import app.JsonApiSearchResult
import app.jQueryUiTermParam
import app.pages.errorPage

fun groupSearch(ctx: Context) {
    val searchTerm = ctx.queryParam(jQueryUiTermParam)

    if (searchTerm == null) {
        ctx.html(errorPage(ctx))
        return
    }

    val result = DataLayer.ValuationGroups.query(searchTerm, 50).map({ JsonApiSearchResult("${it.id}", it.name) })

    ctx.json(result)
}
