package app

import io.javalin.http.Context
import kotlinx.html.*
import app.pages.Body
import app.pages.Head
import app.pages.Page
import app.pages.errorPage
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

fun handleFormSubmit(ctx: Context) {
    val author = ctx.formParam("author-field")
    val comment = ctx.formParam("comment-field")

    if (author == null || comment == null) {
        ctx.html(errorPage(ctx))
        return
    }

    transaction {
        GuestBookTable.insert {
            it[GuestBookTable.id] = UUID.randomUUID()
            it[GuestBookTable.comment] = comment
            it[GuestBookTable.author] = author
        }
    }

    ctx.redirect("/guestbook")
}

fun guestBookPage(ctx: Context) {
    val records = transaction {
        GuestBookTable.selectAll().toList()
    }

    ctx.html(
        Page {
            Head {
                title {
                    + "Welcome to guestbook"
                }
            }

            Body(ctx) {
                h2 {
                    + "Existing records"
                }

                if (records.isEmpty()) {
                    p {
                        + "no records yet"
                    }
                } else {
                    table {
                        attributes["border"] = "1"

                        thead {
                            tr {
                                th { + "Author" }
                                th { + "Comment" }
                            }
                        }

                        tbody {
                            for (record in records) {
                                tr {
                                    td {
                                        + record[GuestBookTable.author]
                                    }

                                    td {
                                        + record[GuestBookTable.comment]
                                    }
                                }
                            }
                        }
                    }
                }

                h2 {
                    + "Add record"
                }

                form {
                    method = FormMethod.post
                    action = "/guestbook"

                    label {
                        + "Author"
                    }

                    br

                    input {
                        name = "author-field"
                        type = InputType.text
                    }

                    br
                    br

                    label {
                        + "Comment"
                    }

                    br

                    textArea {
                        name = "comment-field"
                    }

                    br
                    br

                    button {
                        type = ButtonType.submit

                        + "submit form"
                    }
                }
            }
        }
    )
}
