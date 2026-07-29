package com.restrusher.partypuzl.data.proxies

import com.restrusher.partypuzl.data.local.entities.QuestionPackEntity
import kotlinx.coroutines.flow.Flow

interface QuestionPackProxy {
    fun getPacks(): Flow<List<QuestionPackEntity>>
    suspend fun getPacksOnce(): List<QuestionPackEntity>
    suspend fun seedMissing(packs: List<QuestionPackEntity>)
    suspend fun setEnabled(id: String, enabled: Boolean)
    suspend fun setUnlocked(id: String, unlocked: Boolean)
    suspend fun unlockAllPremium()
    suspend fun deleteRetiredCatalogPacks(keepIds: List<String>)
}
