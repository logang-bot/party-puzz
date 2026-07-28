package com.restrusher.partypuzl.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.restrusher.partypuzl.data.models.PackCategory
import com.restrusher.partypuzl.data.models.PackTier

/**
 * Persisted state of a question pack.
 *
 * The *content* of a built-in pack (its name, icon, colour and prompt arrays) lives in
 * `QuestionPackCatalog`, not here — shipping prompts through Room would make them
 * untranslatable. This table only stores what the user changed: whether the pack is switched
 * on for their next game, and whether a premium pack has been permanently unlocked.
 *
 * [id] matches the catalog id (e.g. `official_truth_or_dare`) so the two can be joined.
 */
@Entity(tableName = "question_packs")
data class QuestionPackEntity(
    @PrimaryKey
    val id: String,
    val tier: PackTier,
    val category: PackCategory,
    val isEnabled: Boolean,
    /**
     * Permanent unlock. Only meaningful for [PackTier.PREMIUM]. A rewarded-ad unlock is
     * deliberately *not* stored here — it lasts for the session only and lives in
     * `SessionUnlocksSource`.
     */
    val isUnlocked: Boolean = false
)
