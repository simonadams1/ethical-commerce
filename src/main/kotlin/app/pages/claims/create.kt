package app.pages.claims

import io.javalin.http.Context
import kotlinx.html.*
import app.*
import app.ui_components.ALERT_TYPE
import app.ui_components.Alert
import app.pages.*
import app.ui_components.FormGroup
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

fun claimCreateOrEditForm(
    ctx: Context,
    claim: Claim? = null,
    message: String? = null,

    /**
     * Set to true when intending to create another claim based on an existing one.
     */
    templateMode: Boolean = false
) {
    val claimTypes = DataLayer.ClaimTypes.getAll()
    val tagsValue = if (claim == null) listOf() else DataLayer.ClaimTags.get(claim.id)

    ctx.html(
        Page {
            Head {
                title {
                    + gettext("Add claim")
                }
            }

            Body(ctx) {

                if (message != null) {
                    Alert(message, ALERT_TYPE.SUCCESS, true)
                }

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
                            value = if (templateMode || claim?.id == null) "" else "${claim.id}"
                        }

                        FormGroup(gettext("Actor")) {
                            AutocompleteFromRemote(
                                app.pages.parties.Urls.Parties.search,
                                actorField,
                                if (claim?.actor == null) null else JsonApiSearchResult("${claim.actor.id}", claim.actor.name)
                            )
                        }

                        FormGroup(gettext("Target")) {
                            AutocompleteFromRemote(
                                app.pages.parties.Urls.Parties.search,
                                targetField,
                                if (claim?.target == null) null else JsonApiSearchResult("${claim.target.id}", claim.target.name)
                            )
                        }

                        FormGroup(gettext("Type")) {
                            select("form-select") {
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

                        FormGroup(gettext("Cause")) {
                            AutocompleteFromRemote(
                                app.pages.causes.Urls.Causes.search,
                                causeField,
                                if (claim?.cause == null) null else JsonApiSearchResult("${claim.cause.id}", claim.cause.name)
                            )
                        }

                        FormGroup(gettext("Description")) {
                            textArea("form-control") {
                                name = descriptionField
                                classes = setOf("form-control")

                                + (claim?.description ?: "")
                            }
                        }

                        FormGroup(gettext("Tags (separated by space")) {
                            input {
                                type = InputType.text
                                name = tagsField
                                value = tagsValue.joinToString(" ") { it.name }
                                classes = setOf("form-control")
                            }
                        }

                        FormGroup(gettext("Source")) {
                            input {
                                type = InputType.text
                                value = claim?.source ?: ""
                                name = sourceField
                                required = true
                                autoComplete = false
                                classes = setOf("form-control")
                            }
                        }

                        FormGroup(gettext("Date")) {
                            input {
                                type = InputType.date
                                value = claim?.happened_at?.toString("yyyy-MM-dd") ?: ""
                                name = happenedAtField
                                required = true
                                classes = setOf("form-control")
                            }
                        }

                        button {
                            type = ButtonType.submit
                            classes = setOf("btn btn-primary btn-lg")

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

fun claimCreateSimilarForm(ctx: Context) {
    val claimId = ctx.pathParam(claimIdPlaceholder)

    val claim = DataLayer.Claims.getById(UUID.fromString(claimId))

    claimCreateOrEditForm(ctx, claim, null, true)
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

    val user = Helpers.getUserFromContext(ctx)

    if (
        sourceValue == null ||
        tagsValueRaw == null ||
        descriptionValue == null ||
        actorValue == null ||
        targetValue == null ||
        happenedAtValue == null ||
        causeString == null ||
        user == null
    ) {
        ctx.html(errorPage(ctx))
        return
    }

    val tagsValue: List<String> = if (tagsValueRaw.isEmpty()) listOf() else tagsValueRaw.split(",").map { it.trim() }
    val updateMode = itemToUpdateValue != null && itemToUpdateValue.isNotEmpty()

    val claimId = DataLayer.Claims.createOrUpdate(
        actorValue,
        targetValue,
        typeValue,
        causeString,
        sourceValue,
        tagsValue,
        if (descriptionValue.trim().isEmpty()) null else descriptionValue.trim(),
        DateTime.parse(happenedAtValue),
        if (updateMode) UUID.fromString(itemToUpdateValue) else null,
        user.role.id == USER_ROLES.ADMINISTRATOR.id
    )

    if (updateMode) {
        ctx.redirect("${Urls.Claims.index}")
    } else {
        // create mode

        val claim = DataLayer.Claims.getById(claimId)

        claimCreateOrEditForm(ctx, claim, gettext("Item created successfully"), true)
    }
}
