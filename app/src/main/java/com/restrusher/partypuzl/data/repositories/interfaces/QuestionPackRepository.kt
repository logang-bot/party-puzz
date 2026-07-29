package com.restrusher.partypuzl.data.repositories.interfaces

import com.restrusher.partypuzl.data.local.entities.QuestionPackEntity
import kotlinx.coroutines.flow.Flow

interface QuestionPackRepository {
    fun getPacks(): Flow<List<QuestionPackEntity>>
    suspend fun getPacksOnce(): List<QuestionPackEntity>

    /** Inserts any catalog pack the DB doesn't have yet, leaving existing rows untouched. */
    suspend fun seedFromCatalog()

    suspend fun setEnabled(id: String, enabled: Boolean)
    suspend fun setUnlocked(id: String, unlocked: Boolean)
    suspend fun unlockAllPremium()

    /** Removes packs the catalog no longer defines. */
    suspend fun deleteRetiredCatalogPacks(keepIds: List<String>)
}
