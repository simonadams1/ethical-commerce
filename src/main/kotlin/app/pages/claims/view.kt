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
import java.net.URL
import java.util.UUID

fun ClaimsTable(
    ctx: Context,
    claims: List<Claim>,
    valuations: Map<UUID, ValuationU>,
    getActions: (claim: Claim) -> List<DropdownOption>
): FlowContent.() -> Unit {
    return {
        div {
            classes = setOf("claims-list")

            for (claim in claims) {
                div {
                    classes = setOf("claim-card")

                    div {
                        classes = setOf("app--flex-block", "claim-card--primary-row")

                        span { + claim.actor.name }

                        span {
                            div {
                                classes = setOf("claim-card--position")

                                if (claim.cause_supports) {
                                    + gettext("Supports")
                                } else {
                                    + gettext("Opposes")
                                }
                            }
                        }

                        span { + claim.cause.name }
                    }

                    div {
                        classes = setOf("app--flex-block", "claim-card--second-row")

                        div {
                            input {
                                type = InputType.date
                                classes = setOf("date-small--readonly")
                                value = claim.happened_at.toString("yyyy-MM-dd")
                                readonly = true
                                required = true
                            }

                            dl {
                                if (claim.target != null) {
                                    dt { + gettext("Target") }
                                    dd { + claim.target.name }
                                }

                                if (claim.description != null) {
                                    dt { + gettext("Description") }
                                    dd { + claim.description }
                                }

                                if (claim.tags.isNotEmpty()) {
                                    dt { + gettext("Tags") }
                                    dd { + claim.tags.joinToString(" ") { "#${it.name}" } }
                                }
                            }
                        }

                        div {
                            DropdownIconOnly(
                                ctx,
                                gettext("Actions"),
                                "three-dots-vertical",
                                listOf(DropdownOption(gettext("Source"), URL(claim.source))) + getActions(claim)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun memberActions(claim: Claim): List<DropdownOption> {
    return listOf(
        DropdownOption(gettext("Add similar"), Urls.Claims.addSimilar("${claim.id}"))
    )
}

fun adminActions(claim: Claim): List<DropdownOption> {
    return listOf(
        DropdownOption(gettext("Edit"), Urls.Claims.getEditPath("${claim.id}")),
        DropdownOption(gettext("Delete"), Urls.Claims.getDeletePath("${claim.id}")),
        DropdownOption(gettext("Add similar"), Urls.Claims.addSimilar("${claim.id}"))
    )
}

fun emptyActions(claim: Claim): List<DropdownOption> {
    return listOf()
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
        ::adminActions
    } else if (hasMinRole(ctx, USER_ROLES.MEMBER)) {
        ::memberActions
    } else {
        ::emptyActions
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

                    ClaimsTable(ctx, claims, valuations, ::emptyActions)()
                }
            }
        )
    }
}
