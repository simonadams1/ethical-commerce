package app.pages.claims

import io.javalin.http.Context
import kotlinx.html.*
import app.*
import app.pages.AutocompleteFromRemote
import app.pages.Body
import app.pages.Head
import app.pages.Page
import app.pages.errorPage
import org.joda.time.DateTime
import java.util.*

const val actorField = "actor"
const val targetField = "target"
const val typeField = "type"
const val causeField = "cause"
const val tagsField = "tags"
const val descriptionField = "description"
const val sourceField = "source"
const val happenedAtField = "happenedAt"

const val itemToUpdateField = "itemToUpdate"

fun claimCreateOrEditForm(ctx: Context, claim: Claim? = null) {
    val claimTypes = DataLayer.ClaimTypes.getAll()

    ctx.html(
        Page {
            Head {
                title {
                    + gettext("Add claim")
                }
            }

            Body(ctx) {
                if (claimTypes.isEmpty()) {
                    div {
                        + gettext("There are no claims types registered.")
                    }
                } else {
                    form {
                        method = FormMethod.post
                        action = "${Urls.Claims.add}"

                        input {
                            type = InputType.hidden
                            name = itemToUpdateField
                            value = if (claim?.id == null) "" else "${claim.id}"
                        }

                        div {
                            label {
                                + gettext("Actor")

                                AutocompleteFromRemote(
                                    app.pages.parties.Urls.Parties.search,
                                    actorField,
                                    if (claim?.actor == null) null else JsonApiSearchResult("${claim.actor.id}", claim.actor.name)
                                )
                            }
                        }

                        div {
                            label {
                                + gettext("Target")

                                AutocompleteFromRemote(
                                    app.pages.parties.Urls.Parties.search,
                                    targetField,
                                    if (claim?.target == null) null else JsonApiSearchResult("${claim.target.id}", claim.target.name)
                                )
                            }
                        }

                        div {
                            label {
                                + gettext("Type")

                                select {
                                    name = typeField
                                    required = true

                                    for (claimType in claimTypes) {
                                        option {
                                            value = claimType.id.toString()
                                            selected = if (claim?.type == null) false else claimType.id == claim.type.id

                                            + claimType.name
                                        }
                                    }
                                }
                            }
                        }

                        div {
                            label {
                                + gettext("Cause")

                                AutocompleteFromRemote(
                                    app.pages.causes.Urls.Causes.search,
                                    causeField,
                                    if (claim?.cause == null) null else JsonApiSearchResult("${claim.cause.id}", claim.cause.name)
                                )
                            }
                        }

                        div {
                            label {
                                + gettext("Description")

                                textArea {
                                    name = descriptionField
                                    required = true

                                    + (claim?.description ?: "")
                                }
                            }
                        }

                        div {
                            label {
                                + gettext("Tags (separated by space, CAN'T BE EDITED)")

                                input {
                                    type = InputType.text
                                    name = tagsField
                                }
                            }
                        }

                        div {
                            label {
                                + gettext("Source")

                                input {
                                    type = InputType.text
                                    value = claim?.source ?: ""
                                    name = sourceField
                                    required = true
                                    autoComplete = false
                                }
                            }
                        }

                        div {
                            label {
                                + gettext("Date")

                                input {
                                    type = InputType.date
                                    value = claim?.happened_at?.toString("yyyy-MM-dd") ?: ""
                                    name = happenedAtField
                                    required = true
                                }
                            }
                        }

                        button {
                            type = ButtonType.submit

                            + gettext("Submit")
                        }
                    }
                }
            }
        }
    )
}

fun claimCreateForm(ctx: Context) {
    claimCreateOrEditForm(ctx)
}

fun claimEditForm(ctx: Context) {
    val id = ctx.pathParam(claimIdPlaceholder)

    val claim = DataLayer.Claims.getById(UUID.fromString(id))

    claimCreateOrEditForm(ctx, claim)
}

fun claimCreateFormHandler(ctx: Context) {
    val actorValue = ctx.formParam(actorField)
    val targetValue = ctx.formParam(targetField)
    val typeValue = UUID.fromString(ctx.formParam(typeField))
    val causeString = ctx.formParam(causeField)
    val sourceValue = ctx.formParam(sourceField)
    val tagsValueRaw = ctx.formParam(tagsField)
    val descriptionValue = ctx.formParam(descriptionField)
    val happenedAtValue = ctx.formParam(happenedAtField)
    val itemToUpdateValue = ctx.formParam(itemToUpdateField)

    if (
        sourceValue == null ||
        tagsValueRaw == null ||
        actorValue == null ||
        targetValue == null ||
        descriptionValue == null ||
        happenedAtValue == null ||
        causeString == null
    ) {
        ctx.html(errorPage(ctx))
        return
    }

    val tagsValue: List<String> = if (tagsValueRaw.length < -1) listOf() else tagsValueRaw.split(" ")

    DataLayer.Claims.createOrUpdate(
        actorValue,
        targetValue,
        typeValue,
        causeString,
        sourceValue,
        tagsValue,
        descriptionValue,
        DateTime.parse(happenedAtValue),
        if (itemToUpdateValue == null || itemToUpdateValue.isEmpty()) null else UUID.fromString(itemToUpdateValue)
    )

    ctx.redirect("${Urls.Claims.index}")
}
