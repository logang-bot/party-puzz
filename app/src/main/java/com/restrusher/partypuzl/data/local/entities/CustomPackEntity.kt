package com.restrusher.partypuzl.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.restrusher.partypuzl.data.models.PackCategory
import com.restrusher.partypuzl.data.models.SpiceLevel

/**
 * The parts of a user-authored pack that a built-in pack keeps in `QuestionPackCatalog`.
 *
 * A custom pack still gets a `question_packs` row — that is what carries the tier and the on/off
 * flag, and it is what the setup screen and the content loader join against. This table only adds
 * the things a catalog entry would have supplied: a name the user typed (so it is *not* a string
 * resource and *not* translatable), a description, and the spice level that picks the icon and
 * accent.
 *
 * [packId] is the same value as `question_packs.id`, so deleting the pack row cascades this away.
 */
@Entity(
    tableName = "custom_packs",
    foreignKeys = [
        ForeignKey(
            entity = QuestionPackEntity::class,
            parentColumns = ["id"],
            childColumns = ["packId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CustomPackEntity(
    @PrimaryKey
    val packId: String,
    val name: String,
    val description: String,
    /**
     * The category the author declared. A label only: the loader pools by each *entry's* type, so
     * a pack may hold entries of any type regardless of what this says.
     */
    val category: PackCategory,
    val spice: SpiceLevel,
    val createdAt: Long,
    /**
     * Whether the pack is offered on the setup screen at all — the switch in the pack manager.
     *
     * Deliberately *not* the same thing as `question_packs.isEnabled`, which is the per-session
     * pick made on the setup screen itself. A built-in pack only needs the second, because the
     * catalog decides which packs exist; an authored pack needs both, so the user can retire one
     * without deleting it and without it cluttering every future setup screen.
     *
     * The SQL default is declared, not just the Kotlin one: `MIGRATION_10_11` adds the column with
     * `DEFAULT 1`, and Room compares default values when it validates the schema on open.
     */
    @ColumnInfo(defaultValue = "1")
    val isAvailable: Boolean = true
)
