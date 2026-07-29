package com.restrusher.partypuzl.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
