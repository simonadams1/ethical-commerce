package app.pages.claims

import io.javalin.http.Context
import kotlinx.html.*
import app.Claim
import app.DataLayer
import app.Helpers
import app.Helpers.hasMinRole
import app.Pagination
import app.PaginationInfo
import app.USER_ROLES
import app.Valuation
import app.gettext
import app.pages.Body
import app.pages.Head
import app.pages.Page
import app.pages.SelectFromRemote
import app.pages.notFoundPage
import java.util.UUID

data class ClaimAction(
    val label: String,
    val getContent: (claim: Claim) -> FlowContent.() -> Unit
)

fun ClaimsTable(claims: List<Claim>, valuations: Map<UUID, Valuation>, actions: Set<ClaimAction> = setOf()): FlowContent.() -> Unit {
    return {
        table {
            classes = setOf("app-table")

            thead {
                tr {
                    th { + gettext("Actor") }
                    th { + gettext("Target") }
                    th { + gettext("Action type") }
                    th { + gettext("Cause") }
                    th { + gettext("Description") }
                    th { + gettext("Link") }
                    th { + gettext("Happened at") }
                    th { + gettext("Conclusion") }

                    for (action in actions) {
                        th { + action.label }
                    }
                }
            }
            tbody {
                for (claim in claims) {
                    val valuation = valuations.get(claim.cause.id)

                    tr {
                        td {
                            if (valuation != null) {
                                if (valuation.isSupporting == claim.type.isSupporting) {
                                    classes = setOf("app--valuation-positive")
                                } else {
                                    classes = setOf("app--valuation-negative")
                                }
                            }

                            + claim.actor.name
                        }
                        td { + (claim.target?.name ?: "") }
                        td { + claim.type.name }
                        td { + claim.cause.name }
                        td { + claim.description }
                        td {
                            a {
                                href = claim.source
                                + gettext("source")
                            }
                        }
                        td {
                            input {
                                type = InputType.date
                                value = claim.happened_at.toString("yyyy-MM-dd")
                                readonly = true
                                required = true
                            }
                        }

                        td {
                            pre {
                                + "[${claim.actor.name}]"

                                + " "

                                if (claim.type.isSupporting) {
                                    + gettext("Supports")
                                } else {
                                    + gettext("Opposes")
                                }

                                + " "

                                + "[${claim.cause.name}]"
                            }
                        }

                        for (action in actions) {
                            td {
                                action.getContent(claim)()
                            }
                        }
                    }
                }
            }
        }
    }
}

fun memberActions(claim: Claim): FlowContent.() -> Unit {
    return {
        a {
            href = "${Urls.Claims.addSimilar("${claim.id}")}"

            + gettext("Add similar")
        }
    }
}

fun adminActions(claim: Claim): FlowContent.() -> Unit {
    return {
        a {
            href = "${Urls.Claims.getEditPath("${claim.id}")}"

            + gettext("Edit")
        }

        + " "

        a {
            href = "${Urls.Claims.getDeletePath("${claim.id}")}"

            + gettext("Delete")
        }

        + " "

        a {
            href = "${Urls.Claims.addSimilar("${claim.id}")}"

            + gettext("Add similar")
        }
    }
}

fun viewClaims(ctx: Context) {
    val partyIdParam = ctx.queryParam(partyIdQueryParam)
    val party = if (partyIdParam == null) null else Helpers.parseUUID(partyIdParam)
    val currentUser = Helpers.getUserFromContext(ctx)

    val pagination = PaginationInfo(
        DataLayer.Claims.getCount(),
        ctx.queryParam("page")?.toInt() ?: 1
    )

    val claims = if (party == null) {
        DataLayer.Claims.getSome(pagination.offset, pagination.pageSize)
    } else {
        DataLayer.Claims.getByParty(party)
    }

    val valuations = if (currentUser == null) {
        mapOf()
    } else {
        DataLayer.Valuations.getUserValuationsForCauses(currentUser.id, claims.map { it.cause.id })
    }

    val editDeleteActions = if (hasMinRole(ctx, USER_ROLES.ADMINISTRATOR)) {
        setOf(ClaimAction("Actions", ::adminActions))
    } else if (hasMinRole(ctx, USER_ROLES.MEMBER)) {
        setOf(ClaimAction("Actions", ::memberActions))
    } else {
        setOf()
    }

    ctx.html(
        Page {
            Head {
                title {
                    + gettext("Claims")
                }
            }

            Body(ctx) {
                h1 {
                    + gettext("Claims")
                }

                div {
                    a {
                        href = "${Urls.Claims.add}"

                        if (!hasMinRole(ctx, USER_ROLES.MEMBER)) {
                            attributes["data-stop-message"] = gettext("Create an account to add claims")
                        }

                        + gettext("Add new")
                    }

                    br {}
                    br {}
                }

                form {
                    method = FormMethod.get
                    action = "${Urls.Claims.index}"

                    label {
                        + gettext("Filter by party")

                        SelectFromRemote(
                            app.pages.parties.Urls.Parties.search,
                            partyIdQueryParam
                        )
                    }

                    input {
                        type = InputType.submit
                        value = gettext("filter")
                    }

                    br {}
                    br {}
                }

                ClaimsTable(claims, valuations, editDeleteActions)()

                br {}
                br {}

                Pagination(ctx, pagination)
            }
        }
    )
}

fun viewActorPositions(ctx: Context) {
    val partyIdParam = ctx.queryParam(partyIdQueryParam)
    val party = if (partyIdParam == null) null else Helpers.parseUUID(partyIdParam)

    val pagination = PaginationInfo(
        DataLayer.Claims.getCount(),
        ctx.queryParam("page")?.toInt() ?: 1
    )

    val claims = if (party == null) {
        DataLayer.Claims.getSome(pagination.offset, pagination.pageSize)
    } else {
        DataLayer.Claims.getByParty(party)
    }

    ctx.html(
        Page {
            Head {
                title {
                    + gettext("Claims")
                }
            }

            Body(ctx) {
                h1 {
                    + gettext("Conclusions")
                }

                form {
                    method = FormMethod.get
                    action = "${Urls.Claims.actorPositions}"

                    label {
                        + gettext("Filter by party")

                        SelectFromRemote(
                            app.pages.parties.Urls.Parties.search,
                            partyIdQueryParam
                        )
                    }

                    input {
                        type = InputType.submit
                        value = gettext("filter")
                    }

                    br {}
                    br {}
                }

                table {
                    classes = setOf("app-table")

                    thead {
                        tr {
                            th { + gettext("Actor") }
                            th { + gettext("Stance") }
                            th { + gettext("Cause") }
                            th { + gettext("Source") }
                        }
                    }

                    tbody {
                        for (claim in claims) {
                            tr {
                                td { + claim.actor.name }
                                td {
                                    if (claim.type.isSupporting) {
                                        + gettext("Supports")
                                    } else {
                                        + gettext("Opposes")
                                    }
                                }
                                td { + claim.cause.name }
                                td {
                                    a {
                                        href = Urls.Claims.singleClaim("${claim.id}").path

                                        + gettext("source")
                                    }
                                }
                            }
                        }
                    }
                }

                br {}
                br {}

                Pagination(ctx, pagination)
            }
        }
    )
}

fun viewSingleClaim(ctx: Context) {
    val idString = ctx.pathParam(claimIdPlaceholder)
    val id = Helpers.parseUUID(idString)

    if (id == null) {
        notFoundPage(ctx)
    } else {
        val claim = DataLayer.Claims.getById(id)
        val claims = listOf(claim)

        val currentUser = Helpers.getUserFromContext(ctx)

        val valuations = if (currentUser == null) {
            mapOf()
        } else {
            DataLayer.Valuations.getUserValuationsForCauses(currentUser.id, claims.map { it.cause.id })
        }

        ctx.html(
            Page {
                Head {
                    title {
                        + gettext("Claim")
                    }
                }

                Body(ctx) {
                    h1 {
                        + gettext("Claim")
                    }

                    ClaimsTable(claims, valuations)()
                }
            }
        )
    }
}
