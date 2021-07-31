package app

import io.javalin.core.security.Role
import io.javalin.http.Context
import java.net.URL
import java.util.UUID

fun gettext(label: String, placeholders: Map<String, String> = mapOf()): String {
    var currentLabel = label

    for (p in placeholders) {
        currentLabel = currentLabel.replace("{{${p.key}}}", p.value)
    }

    return currentLabel
}

object Helpers {
    private val baseUrlString = System.getenv("HOST")
    private val baseUrl = URL(baseUrlString)

    fun getUrl(path: String): URL {
        return URL(baseUrl, path)
    }

    fun parseUUID(str: String?): UUID? {
        if (str == null) {
            return null
        }

        var uuid: UUID? = null

        try {
            uuid = UUID.fromString(str)
        } catch(_: Error) {
            // will return null below
        }

        return uuid
    }

    fun rolesBelow(role: USER_ROLES): Set<Role> {
        return USER_ROLES.values().filter { it.power <= role.power }.toSet()
    }

    fun rolesAbove(role: USER_ROLES): Set<Role> {
        return USER_ROLES.values().filter { it.power >= role.power }.toSet()
    }

    fun hasMinRole(ctx: Context, role: USER_ROLES): Boolean {
        val user = getUserFromContext(ctx)
        val currentRole = if (user == null) USER_ROLES.GUEST else user.role

        return currentRole.power >= role.power
    }

    fun getUserFromContext(ctx: Context): User? {
        val sessionUser = ctx.attribute<User>(SESSION_USER)

        if (sessionUser != null) {
            return sessionUser
        }

        val loginToken = ctx.cookie(LOGIN_COOKIE_NAME)

        if (loginToken == null) {
            return null
        }

        val user = DataLayer.Users.getByLoginToken(UUID.fromString(loginToken))

        if (user == null) {
            ctx.removeCookie(LOGIN_COOKIE_NAME, "/")
            return null
        } else {
            ctx.attribute(SESSION_USER, user)

            return user
        }
    }

    fun logout(ctx: Context) {
        val user = getUserFromContext(ctx)

        if (user != null) {
            DataLayer.Users.updateLoginToken(user.id, null)
            ctx.removeCookie(LOGIN_COOKIE_NAME)
        }
    }

    fun getId(ctx: Context): String {
        var latest = ctx.attribute<Int>("ui-id")

        if (latest == null) {
            latest = 0
        } else {
            latest += 1
        }

        ctx.attribute("ui-id", latest)

        return "gid$latest"
    }
}

data class WebPage(
    val name: String,
    val url: URL,
    val roles: Set<Role> = setOf()
)

object Navigation {
    val pages = ArrayList<WebPage>()

    fun addPage(page: WebPage) {
        pages.add(page)
    }
}

data class JsonApiSearchResult(
    val id: String,
    val label: String
)

val jQueryUiTermParam = "term"
