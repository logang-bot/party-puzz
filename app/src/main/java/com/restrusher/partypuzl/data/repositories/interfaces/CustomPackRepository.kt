package com.restrusher.partypuzl.data.repositories.interfaces

import com.restrusher.partypuzl.data.local.dao.CustomPackSummary
import com.restrusher.partypuzl.data.local.entities.CustomEntryEntity
import com.restrusher.partypuzl.data.models.CustomEntryDraft
import com.restrusher.partypuzl.data.models.CustomPackDraft
import kotlinx.coroutines.flow.Flow

interface CustomPackRepository {
    fun getSummaries(): Flow<List<CustomPackSummary>>
    fun getSummary(packId: String): Flow<CustomPackSummary?>
    suspend fun getSummaryOnce(packId: String): CustomPackSummary?

    /** Creates the pack and returns its new id, or updates it when the draft carries one. */
    suspend fun savePack(draft: CustomPackDraft): String
    suspend fun deletePack(packId: String)
    /**
     * The manager's switch: whether the pack is offered on the setup screen at all. The
     * per-session pick made there is `QuestionPackRepository.setEnabled` instead.
     */
    suspend fun setAvailable(packId: String, available: Boolean)

    fun getEntries(packId: String): Flow<List<CustomEntryEntity>>
    suspend fun getEntry(id: String): CustomEntryEntity?
    suspend fun saveEntry(draft: CustomEntryDraft)
    suspend fun deleteEntry(id: String)

    /** Entries from packs switched on for this game — the custom half of the playable deck. */
    suspend fun getPlayableEntries(): List<CustomEntryEntity>
}
