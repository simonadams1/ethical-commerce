package app.pages.parties

import io.javalin.http.Context
import kotlinx.html.*
import app.DataLayer
import app.gettext
import app.pages.Body
import app.pages.Head
import app.pages.Page

fun viewParties(ctx: Context) {
    val parties = DataLayer.Parties.getAll()

    ctx.html(
        Page {
            Head {
                title {
                    + gettext("Untitled")
                }
            }

            Body(ctx) {
                h1 {
                    + gettext("Parties")
                }

                div {
                    a {
                        href = "${Urls.Parties.create}"

                        + gettext("Add new")
                    }

                    br {}
                    br {}
                }

                table {
                    classes = setOf("app-table")

                    thead {
                        tr {
                            th { + gettext("ID") }
                            th { + gettext("Name") }
                            th { + "" }
                        }
                    }
                    tbody {
                        for (party in parties) {
                            tr {
                                td { + party.id.toString() }
                                td { + party.name }
                                td {
                                    form {
                                        action = "${Urls.Parties.delete}"
                                        method = FormMethod.post

                                        input {
                                            type = InputType.hidden
                                            name = idField
                                            value = "${party.id}"
                                        }

                                        button {
                                            type = ButtonType.submit

                                            + gettext("Delete")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
