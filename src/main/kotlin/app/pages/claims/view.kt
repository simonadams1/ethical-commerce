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
import app.ui_components.*
import java.util.UUID

data class ClaimAction(
    val label: String,
    val getContent: (claim: Claim) -> FlowContent.() -> Unit
)

fun ClaimsTable(ctx: Context, claims: List<Claim>, valuations: Map<UUID, Valuation>, actions: Set<ClaimAction> = setOf()): FlowContent.() -> Unit {
    val debug_claims = false

    return {
        table {
            classes = setOf("table table-bordered")

            thead {
                tr {
                    th { + gettext("Actor") }
                    th { + gettext("Target") }
                    th { + gettext("Action type") }
                    th { + gettext("Cause") }
                    th { + gettext("Details") }

                    if (debug_claims) {
                        th { + gettext("Conclusion") }
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
                        td {
                            span("app--block") {
                                + claim.cause.name
                            }

                            if (claim.description != null) {
                                br
                                Collapsible(ctx,gettext("more")) {
                                    p {
                                        + claim.description
                                    }
                                }
                            }
                        }
                        td {
                            span("app--block") {
                                input {
                                    type = InputType.date
                                    value = claim.happened_at.toString("yyyy-MM-dd")
                                    readonly = true
                                    required = true
                                }
                            }

                            span("app--block") {
                                a {
                                    href = claim.source
                                    + gettext("source")
                                }
                            }

                            span("app--block") {
                                + claim.tags.joinToString(" ") { "#${it.name}" }
                            }

                            span("app--block") {
                                span {
                                    for (action in actions) {
                                        span("app--block") {
                                            action.getContent(claim)()
                                        }
                                    }
                                }
                            }
                        }


                        if (debug_claims) {
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
                FlexBlock {
                    h1 {
                        + gettext("Claims")
                    }

                    LinkButton(
                        gettext("Add new"),
                        Urls.Claims.add,
                        BUTTON_STYLE.PRIMARY,
                        if (hasMinRole(ctx, USER_ROLES.MEMBER)) {
                            mapOf()
                        } else {
                            mapOf("data-stop-message" to "Create an account to add claims")
                        }
                    )
                }

                br

                form {
                    method = FormMethod.get
                    action = "${Urls.Claims.index}"
                    classes = setOf("row")

                    div("col-auto") {
                        SelectFromRemote(
                            app.pages.parties.Urls.Parties.search,
                            partyIdQueryParam,
                            null,
                            gettext("Filter by party")
                        )
                    }

                    div("col-auto") {
                        button {
                            type = ButtonType.submit
                            classes = setOf("btn btn-outline-secondary")

                            + gettext("Filter")
                        }
                    }
                }

                br

                ClaimsTable(ctx, claims, valuations, editDeleteActions)()

                br
                br

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

                br

                form {
                    method = FormMethod.get
                    action = "${Urls.Claims.actorPositions}"
                    classes = setOf("row")

                    div("col-auto") {
                        SelectFromRemote(
                            app.pages.parties.Urls.Parties.search,
                            partyIdQueryParam,
                            null,
                            gettext("Filter by party")
                        )
                    }

                    div("col-auto") {
                        button {
                            type = ButtonType.submit
                            classes = setOf("btn btn-outline-secondary")

                            + gettext("Filter")
                        }
                    }
                }

                br

                table {
                    classes = setOf("table table-bordered")

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

                br

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

                    ClaimsTable(ctx, claims, valuations)()
                }
            }
        )
    }
}
