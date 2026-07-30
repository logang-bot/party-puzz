package com.restrusher.partypuzl.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.restrusher.partypuzl.data.local.entities.CustomEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomEntryDao {

    /** Authoring order, so the editor lists entries the way they were written. */
    @Query("SELECT * FROM custom_entries WHERE packId = :packId ORDER BY position ASC")
    fun getForPack(packId: String): Flow<List<CustomEntryEntity>>

    @Query("SELECT * FROM custom_entries WHERE id = :id")
    suspend fun getById(id: String): CustomEntryEntity?

    /**
     * Entries belonging to packs the user has switched on, and not individually muted. The custom
     * mirror of `QuestionDao.getPlayableQuestions()`.
     *
     * `isAvailable` is checked here as well as by `CustomPackDao.setAvailability`, which already
     * clears `isEnabled` when a pack is withdrawn. That is deliberate: a pack the user has taken
     * off the setup screen must not be able to deal, and making that structural rather than
     * trusting one transaction to have run is the same guarantee `deleteRetiredCatalogPacks` gets
     * from its `tier != 'CUSTOM'` clause.
     */
    @Query(
        """
        SELECT e.* FROM custom_entries e
        INNER JOIN question_packs p ON p.id = e.packId
        INNER JOIN custom_packs c ON c.packId = e.packId
        WHERE p.isEnabled = 1 AND c.isAvailable = 1 AND e.isEnabled = 1
        """
    )
    suspend fun getPlayableEntries(): List<CustomEntryEntity>

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM custom_entries WHERE packId = :packId")
    suspend fun nextPosition(packId: String): Int

    @Upsert
    suspend fun upsert(entry: CustomEntryEntity)

    @Query("DELETE FROM custom_entries WHERE id = :id")
    suspend fun delete(id: String)
}
