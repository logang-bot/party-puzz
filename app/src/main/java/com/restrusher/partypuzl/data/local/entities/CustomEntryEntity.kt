package com.restrusher.partypuzl.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.restrusher.partypuzl.data.models.CustomEntryType

/**
 * One authored prompt, text and all.
 *
 * This is the one place in the schema that stores prompt text. `QuestionEntity` deliberately does
 * not — it points at a `string-array` so official prompts stay translatable. User-written text has
 * no translation to point at, so it is stored verbatim and bypasses `QuestionPromptResolver`
 * entirely.
 *
 * Keeping it in its own table rather than as nullable columns on `questions` also puts it out of
 * reach of `QuestionDao.replaceAll()`, which wipes that table whenever the catalog's mapping
 * version changes.
 *
 * Only the columns the [type] needs are filled:
 * - [CustomEntryType.STICKY_DARE] uses [durationSeconds]
 * - [CustomEntryType.TRIVIA] uses [optionA], [optionB] and [correctOption]
 * - truths and dares use [text] alone
 */
@Entity(
    tableName = "custom_entries",
    foreignKeys = [
        ForeignKey(
            entity = QuestionPackEntity::class,
            parentColumns = ["id"],
            childColumns = ["packId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("packId")]
)
data class CustomEntryEntity(
    @PrimaryKey
    val id: String,
    val packId: String,
    val type: CustomEntryType,
    val text: String,
    val durationSeconds: Int?,
    val optionA: String?,
    val optionB: String?,
    /**
     * `"A"` or `"B"`, stored as text because Room has no built-in `Char` converter. Widened back
     * to a `Char` at the boundary, where `TriviaPrompt.correctOption` expects one.
     */
    val correctOption: String?,
    /** Per-entry opt-out, mirroring `QuestionEntity.isEnabled`. */
    val isEnabled: Boolean = true,
    /** Authoring order, so the editor lists entries the way they were written. */
    val position: Int = 0
)
