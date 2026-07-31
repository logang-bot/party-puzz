package com.restrusher.partypuzl.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.restrusher.partypuzl.data.local.entities.QuestionPackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionPackDao {

    @Query("SELECT * FROM question_packs")
    fun getAllPacks(): Flow<List<QuestionPackEntity>>

    @Query("SELECT * FROM question_packs")
    suspend fun getAllPacksOnce(): List<QuestionPackEntity>

    /**
     * Seeds rows the catalog defines but the DB has never seen. IGNORE keeps a user's existing
     * toggle/unlock state intact when the catalog grows in a later release.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMissing(packs: List<QuestionPackEntity>)

    /**
     * Brings every catalog row back in line with the catalog after a `CATALOG_VERSION` bump.
     *
     * Only the columns the catalog owns are rewritten. `isEnabled` and `isUnlocked` belong to the
     * user — resetting them would switch a curated pack back on after they turned it off, and
     * would take back a premium unlock they paid for — so [insertMissing] supplies them once, for
     * rows that did not exist, and nothing touches them again.
     */
    @Transaction
    suspend fun resyncCatalog(defaults: List<QuestionPackEntity>) {
        insertMissing(defaults)
        defaults.forEach { updateCatalogFields(it.id, it.tier.name, it.category.name) }
    }

    /** Enum names rather than the enums themselves, matching how the raw queries above read them. */
    @Query("UPDATE question_packs SET tier = :tier, category = :category WHERE id = :id")
    suspend fun updateCatalogFields(id: String, tier: String, category: String)

    @Upsert
    suspend fun upsert(pack: QuestionPackEntity)

    @Query("UPDATE question_packs SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: String, enabled: Boolean)

    @Query("UPDATE question_packs SET isUnlocked = :unlocked WHERE id = :id")
    suspend fun setUnlocked(id: String, unlocked: Boolean)

    /** Used when the "remove ads" purchase lands — it unlocks every premium pack at once. */
    @Query("UPDATE question_packs SET isUnlocked = 1 WHERE tier = 'PREMIUM'")
    suspend fun unlockAllPremium()

    @Query("DELETE FROM question_packs WHERE id = :id")
    suspend fun delete(id: String)

    /**
     * Retires packs the catalog no longer defines; their questions cascade away.
     *
     * Custom packs are exempt: they exist precisely *because* they are not in the catalog, so
     * without the tier guard every user-authored pack would be deleted on the next launch — along
     * with its `custom_packs` and `custom_entries` rows, through the foreign key.
     */
    @Query("DELETE FROM question_packs WHERE tier != 'CUSTOM' AND id NOT IN (:keepIds)")
    suspend fun deleteRetiredCatalogPacks(keepIds: List<String>)
}
