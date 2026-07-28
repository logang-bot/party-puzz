package com.restrusher.partypuzl.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.restrusher.partypuzl.data.models.QuestionSource

/**
 * One question's membership of a pack.
 *
 * The row holds no text. It points at a position in a `string-array` via [source] + [sourceIndex],
 * and `QuestionPromptResolver` reads the text at draw time — so questions stay translatable while
 * their categorisation lives in the database.
 *
 * [source] is an enum, never an `R.array` id: resource ids are regenerated per build and a
 * persisted one can end up pointing at a different array entirely.
 *
 * Deleting a pack cascades to its questions.
 */
@Entity(
    tableName = "questions",
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
data class QuestionEntity(
    /** Deterministic: `"<source>_<index>"`, e.g. `OFFICIAL_TRUTHS_13`. */
    @PrimaryKey
    val id: String,
    val packId: String,
    val source: QuestionSource,
    val sourceIndex: Int,
    /**
     * Per-question opt-out. Nothing toggles this yet — the setup screen works at pack level —
     * but the column is what makes "mute this one question" possible without a migration.
     */
    val isEnabled: Boolean = true
)
