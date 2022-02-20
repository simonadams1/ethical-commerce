package app.pages.claims

import app.*
import io.javalin.http.Context
import kotlinx.html.*
import app.Helpers.hasMinRole
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

fun ClaimsTable(ctx: Context, claims: List<Claim>, valuations: Map<UUID, ValuationU>, actions: Set<ClaimAction> = setOf()): FlowContent.() -> Unit {
    return {
        table {
            classes = setOf("table table-bordered")

            thead {
                tr {
                    th { + gettext("Date") }
                    th { + gettext("Actor") }
                    th { + gettext("Cause") }
                    th { + gettext("Target") }
                    th { + gettext("Details") }
                }
            }
            tbody {
                for (claim in claims) {
                    tr {
                        td {
                            input {
                                type = InputType.date
                                value = claim.happened_at.toString("yyyy-MM-dd")
                                readonly = true
                                required = true
                            }
                        }
                        td { + claim.actor.name }
                        td {
                            span("app--block") {

                                if (claim.cause_supports) {
                                    + "[${gettext("Supports")}] "
                                } else {
                                    + "[${gettext("Opposes")}] "
                                }

                                + claim.cause.name
                            }

                            if (claim.description != null) {
                                br
                                Collapsible(ctx, gettext("more")) {
                                    p {
                                        + claim.description
                                    }
                                }
                            }
                        }
                        td { + (claim.target?.name ?: "") }
                        td {
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

fun getClaimsWithPagination(ctx: Context, party: UUID? = null): Pair<PaginationInfo, List<Claim>> {
    val pagination: PaginationInfo

    val claims = if (party == null) {
        pagination = PaginationInfo(
            DataLayer.Claims.getCount(),
            ctx.queryParam("page")?.toInt() ?: 1
        )

        DataLayer.Claims.getSome(pagination.offset, pagination.pageSize)
    } else {
        pagination = PaginationInfo(
            DataLayer.Claims.getCountByParty(party),
            ctx.queryParam("page")?.toInt() ?: 1
        )

        DataLayer.Claims.getSomeByParty(party, pagination.offset, pagination.pageSize)
    }

    return Pair(pagination, claims)
}

fun viewClaims(ctx: Context) {
    val partyIdParam = ctx.queryParam(partyIdQueryParam)
    val party = if (partyIdParam == null) null else Helpers.parseUUID(partyIdParam)
    val currentUser = Helpers.getUserFromContext(ctx)
    val (pagination, claims) = getClaimsWithPagination(ctx, party)

    val valuations = if (currentUser == null) {
        mapOf()
    } else {
        DataLayer.ValuationsU.getUserValuationsForCauses(currentUser, claims.map { it.cause.id })
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
            DataLayer.ValuationsU.getUserValuationsForCauses(currentUser, claims.map { it.cause.id })
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
