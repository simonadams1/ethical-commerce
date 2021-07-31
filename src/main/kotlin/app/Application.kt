package app

import io.javalin.Javalin
import io.javalin.http.staticfiles.Location
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import app.pages.causes.registerCausesPages
import app.pages.claim_types.registerClaimTypePages
import app.pages.claims.registerClaimsPages
import app.pages.moderation.registerModeration
import app.pages.notFoundPage
import app.pages.parties.registerPartiesPages
import app.pages.users.registerUsersPage
import app.pages.valuation.registerValuation

fun main(args: Array<String>) {

    Class.forName("org.postgresql.Driver")
    Database.connect(
        "jdbc:${ENV.DATABASE_URL}",
        driver = "org.postgresql.Driver",
        user = ENV.DATABASE_USER,
        password = ENV.DATABASE_PASSWORD
    )

    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            UsersTable,
            ClaimTypesTable,
            ClaimTagsTable,
            ClaimTagsReferencesTable,
            PartiesTable,
            ClaimsTable,
            ClaimReasonsTable,
            CausesTable,
            MonitoredPartiesTable,
            UserNotificationsTable,
            ValuationGroupsTable,
            ValuationGroupMembersTable,
            ValuationsByGroupTable,
            ValuationsByUserTable
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

    app.start(ENV.PORT)

    app.get("/test-page", ::testPage)

    registerClaimTypePages(app)
    registerPartiesPages(app)
    registerClaimsPages(app)
    registerCausesPages(app)
    registerUsersPage(app)
    registerModeration(app)

    // TEMPORARILY DISABLED
    // registerWatchParty(app)

    registerValuation(app)

    app.error(404, ::notFoundPage)
}
