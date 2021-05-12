package app.pages.claim_types

import io.javalin.http.Context
import kotlinx.html.*
import app.DataLayer
import app.gettext
import app.pages.Body
import app.pages.Head
import app.pages.Page

fun claimTypesView(ctx: Context) {
    val claimTypes = DataLayer.ClaimTypes.getAll()

    ctx.html(
        Page {
            Head {
                title {
                    + "Untitled"
                }
            }

            Body(ctx) {
                h1 {
                    + gettext("Claim types")
                }

                div {
                    a {
                        href = "${Urls.ClaimTypes.create}"

                        + gettext("Add new")
                    }

                    br {}
                    br {}
                }

                table {
                    classes = setOf("app-table")

                    thead {
                        tr {
                            th {
                                + gettext("ID")
                            }

                            th {
                                + gettext("Name")
                            }

                            th {
                                + gettext("Position")
                            }
                        }
                    }
                    tbody {
                        for (claimType in claimTypes) {
                            tr {
                                td { + claimType.id.toString() }
                                td { + claimType.name }
                                td {
                                    if (claimType.isSupporting) {
                                        + gettext("Supporting")
                                    } else {
                                        + gettext("Opposing")
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
