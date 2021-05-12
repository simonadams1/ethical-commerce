package app

import io.javalin.Javalin
import org.jetbrains.exposed.sql.Database
import app.pages.notFoundPage
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

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
            GuestBookTable
        )
    }

    val app = Javalin.create()

    app.start(ENV.PORT)

    app.get("/guestbook", ::guestBookPage)
    app.post("/guestbook", ::handleFormSubmit)

    app.error(404, ::notFoundPage)
}
