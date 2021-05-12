package app

import io.javalin.Javalin
import io.javalin.http.staticfiles.Location
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import app.pages.causes.registerCausesPages
import app.pages.claim_types.registerClaimTypePages
import app.pages.claims.registerClaimsPages
import app.pages.claims.viewClaims
import app.pages.moderation.registerModeration
import app.pages.notFoundPage
import app.pages.parties.registerPartiesPages
import app.pages.users.registerUsersPage
import app.pages.valuation.registerValuation
import app.pages.watch_party.registerWatchParty

fun main(args: Array<String>) {
    val DATABASE_URL = System.getenv("DB_URL")
    val DATABASE_USER = System.getenv("DATABASE_USER")
    val DATABASE_PASSWORD = System.getenv("DATABASE_PASSWORD")

    Class.forName("org.postgresql.Driver")
    Database.connect("jdbc:$DATABASE_URL", driver = "org.postgresql.Driver", user = DATABASE_USER, password = DATABASE_PASSWORD)

    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            UsersTable,
            ClaimTypesTable,
            PartiesTable,
            ClaimsTable,
            ClaimReasonsTable,
            CausesTable,
            MonitoredPartiesTable,
            UserNotificationsTable,
            ValuationGroupsTable,
            ValuationGroupMembersTable,
            ValuationsTable
        )
    }

    val app = Javalin.create()

    app.config.addStaticFiles("static", Location.EXTERNAL)

    app.config.accessManager { handler, ctx, permittedRoles ->
        if (permittedRoles.size < 1) { // not limited
            handler.handle(ctx)
        } else {
            val user = Helpers.getUserFromContext(ctx)

            if (user != null && permittedRoles.contains(user.role)) {
                handler.handle(ctx)
            } else {
                ctx.status(401).result(gettext("Unauthorized"))
            }
        }
    }

    app.start(System.getenv("PORT")?.toInt() ?: 8080)

    app.get("/", ::viewClaims)
    app.get("/test-page", ::testPage)

    registerClaimTypePages(app)
    registerPartiesPages(app)
    registerClaimsPages(app)
    registerCausesPages(app)
    registerUsersPage(app)
    registerModeration(app)
    registerWatchParty(app)
    registerValuation(app)

    app.error(404, ::notFoundPage)
}
