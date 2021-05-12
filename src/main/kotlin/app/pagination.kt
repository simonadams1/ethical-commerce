package app

import io.javalin.http.Context
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.div
import org.apache.http.NameValuePair
import org.apache.http.client.utils.URLEncodedUtils
import kotlin.math.ceil
import kotlin.math.max

class NameValuePair : NameValuePair {
    private var _name: String = ""
    private var _value: String = ""

    constructor(name: String, value: String) {
        _name = name
        _value = value
    }

    override fun getName(): String {
        return _name
    }

    override fun getValue(): String {
        return _value
    }
}

class PaginationInfo(_itemCount: Int, _currentPage: Int) {
    val itemCount = _itemCount
    val currentPage = _currentPage
    val pageSize = 100
    var offset = (currentPage - 1) * pageSize
    var totalPages = max(ceil(itemCount / pageSize.toDouble()).toInt(), 1)
}

fun FlowContent.Pagination(ctx: Context, info: PaginationInfo) {
    val queryParamsMap = if (ctx.queryString() != null) {
        URLEncodedUtils.parse(ctx.queryString(), charset("utf-8")).associate { it.name to it.value }
    } else {
        mapOf()
    }

    val queryNext = queryParamsMap
        .plus("page" to "${info.currentPage + 1}")
        .toList()
        .map { NameValuePair(it.first, it.second) }

    val queryPrev = queryParamsMap
        .plus("page" to "${info.currentPage - 1}")
        .toList()
        .map { NameValuePair(it.first, it.second) }

    val nextUrl = ctx.url() + "?" + URLEncodedUtils.format(queryNext, "utf-8")
    val prevUrl = ctx.url() + "?" + URLEncodedUtils.format(queryPrev, "utf-8")

    div {
        + gettext("Page {{x}} of {{y}}", mapOf("x" to "${info.currentPage}", "y" to "${info.totalPages.toInt()}"))
        if (info.currentPage > 1) {
            + " "
            a {
                href = prevUrl
                + gettext("prev")
            }
        }
        if (info.currentPage < info.totalPages) {
            + " "
            a {
                href = nextUrl
                + gettext("next")
            }
        }
    }
}