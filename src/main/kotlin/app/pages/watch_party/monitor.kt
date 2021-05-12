package app.pages.watch_party

import io.javalin.http.Context
import kotlinx.html.*
import app.gettext
import app.pages.Body
import app.pages.Head
import app.pages.Page
import app.DataLayer
import app.Helpers
import app.pages.AutocompleteFromRemote
import app.pages.errorPage

val partyNameField = "party"

fun indexView(ctx: Context) {
    val user = Helpers.getUserFromContext(ctx)

    if (user == null) {
        ctx.html(errorPage(ctx))
        return
    }

    val monitoriedParties = DataLayer.MonitoredParties.getAll(user.id)

    ctx.html(
        Page {
            Head {
                title {
                    + gettext("Monitor companies")
                }
            }

            Body(ctx) {
                h2 {
                    + gettext("Monitor companies")
                }
                p {
                    + gettext("Get notified when claims regarding a party get added")
                }

                form {
                    method = FormMethod.post
                    action = "${Urls.MonitorParties.addEntry}"

                    label {
                        + gettext("Party")

                        AutocompleteFromRemote(
                            app.pages.parties.Urls.Parties.search,
                            partyNameField
                        )
                    }

                    div {
                        input {
                            type = InputType.submit
                            value = gettext("Add to list")
                        }
                    }
                }

                if (monitoriedParties.isEmpty()) {
                    p {
                        + gettext("You are not watching any parties")
                    }
                } else {
                    h3 {
                        + gettext("Parties you watch")
                    }

                    ul {
                        for (monitoredParty in monitoriedParties) {
                            li {
                                + monitoredParty.party.name
                            }
                        }
                    }
                }
            }
        }
    )
}

fun handleAddEntry(ctx: Context) {
    val partyNameValue = ctx.formParam(partyNameField)
    val user = Helpers.getUserFromContext(ctx)

    if (partyNameValue == null || user == null) {
        ctx.html(errorPage(ctx))
        return
    }

    DataLayer.MonitoredParties.create(partyNameValue, user.id)

    ctx.redirect("${Urls.MonitorParties.index}")
}
