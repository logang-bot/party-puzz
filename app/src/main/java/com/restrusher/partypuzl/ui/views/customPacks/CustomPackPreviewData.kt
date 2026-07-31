package com.restrusher.partypuzl.ui.views.customPacks

import com.restrusher.partypuzl.data.local.entities.CustomEntryEntity
import com.restrusher.partypuzl.data.models.CustomEntryType
import com.restrusher.partypuzl.data.models.PackTopic
import com.restrusher.partypuzl.data.models.SpiceLevel
import com.restrusher.partypuzl.ui.views.customPacks.create.CreateCustomPackState
import com.restrusher.partypuzl.ui.views.customPacks.editor.CustomPackEditorState
import com.restrusher.partypuzl.ui.views.customPacks.entry.CreateCustomEntryState
import com.restrusher.partypuzl.ui.views.customPacks.list.CustomPackUiModel
import com.restrusher.partypuzl.ui.views.customPacks.list.CustomPacksState
import com.restrusher.partypuzl.ui.views.customPacks.list.PackWarning

/**
 * Sample packs and entries for the `@Preview`s across this feature.
 *
 * Shared rather than repeated per file because the same pack shows up in the manager list, the
 * card, and the editor header, and the previews are easier to read when they all describe the
 * same imaginary party.
 */

internal val previewPack = CustomPackUiModel(
    id = "custom_preview",
    name = "House Rules",
    description = "Inside jokes from the trip — nobody outside the group will get these.",
    topic = PackTopic.FRIENDS_INSIDE_JOKES,
    spice = SpiceLevel.MEDIUM,
    entryCount = 12,
    isAvailable = true,
    warning = PackWarning.TRUTHS_ONLY
)

internal val previewPacks = listOf(
    previewPack,
    CustomPackUiModel(
        id = "custom_preview_2",
        name = "Kitchen Floor Confessions",
        description = "For the part of the night that happens after the music stops.",
        topic = PackTopic.BIG_NIGHT_OUT,
        spice = SpiceLevel.SPICY,
        entryCount = 7,
        isAvailable = false,
        warning = null
    ),
    CustomPackUiModel(
        id = "custom_preview_3",
        name = "Pub Quiz Leftovers",
        description = "",
        topic = PackTopic.FOOD_AND_DRINK,
        spice = SpiceLevel.MILD,
        entryCount = 0,
        isAvailable = true,
        warning = PackWarning.EMPTY
    )
)

internal val previewTrivia = CustomEntryEntity(
    id = "preview_trivia",
    packId = "pack",
    type = CustomEntryType.TRIVIA,
    text = "Which country drinks the most wine per capita?",
    durationSeconds = null,
    optionA = "Portugal",
    optionB = "France",
    correctOption = "A"
)

internal val previewEntries = listOf(
    CustomEntryEntity(
        id = "preview_truth",
        packId = "pack",
        type = CustomEntryType.TRUTH,
        text = "What is the worst thing you have ever said to get out of a round?",
        durationSeconds = null,
        optionA = null,
        optionB = null,
        correctOption = null,
        position = 0
    ),
    CustomEntryEntity(
        id = "preview_sticky",
        packId = "pack",
        type = CustomEntryType.STICKY_DARE,
        text = "End every sentence with “…allegedly.”",
        durationSeconds = 300,
        optionA = null,
        optionB = null,
        correctOption = null,
        position = 1
    ),
    previewTrivia.copy(position = 2)
)

/** A sticky dare mid-write, the state the entry form and its preview card are built around. */
internal val previewEntryState = CreateCustomEntryState(
    packId = "pack",
    packName = "House Rules",
    type = CustomEntryType.STICKY_DARE,
    text = "End every sentence with “…allegedly.”",
    durationSeconds = 300
)

/** A dare mid-write — the path with the step-02 truth/dare toggle showing. */
internal val previewTruthOrDareEntryState = CreateCustomEntryState(
    packId = "pack",
    packName = "House Rules",
    type = CustomEntryType.DARE,
    text = "Call the last number you dialled and sing the chorus."
)

internal val previewTriviaEntryState = CreateCustomEntryState(
    packId = "pack",
    packName = "Pub Quiz Leftovers",
    type = CustomEntryType.TRIVIA,
    text = "Which country drinks the most wine per capita?",
    optionA = "Portugal",
    optionB = "France",
    correctOption = 'A'
)

internal val previewPackFormState = CreateCustomPackState(
    name = "House Rules",
    topic = PackTopic.FRIENDS_INSIDE_JOKES,
    spice = SpiceLevel.MEDIUM,
    description = "Inside jokes from the trip — nobody outside the group will get these."
)

internal val previewPacksState = CustomPacksState(isLoading = false, packs = previewPacks)

internal val previewEditorState = CustomPackEditorState(
    pack = previewPack,
    entries = previewEntries
)
