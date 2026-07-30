package com.restrusher.partypuzl.data.proxies

import com.restrusher.partypuzl.data.local.dao.CustomPackSummary
import com.restrusher.partypuzl.data.local.entities.CustomEntryEntity
import com.restrusher.partypuzl.data.local.entities.CustomPackEntity
import com.restrusher.partypuzl.data.local.entities.QuestionPackEntity
import kotlinx.coroutines.flow.Flow

/**
 * One proxy for both custom-pack tables. They are never read apart — a pack without its entries is
 * not something any screen shows — so splitting them would only duplicate the plumbing.
 */
interface CustomPackProxy {
    fun getSummaries(): Flow<List<CustomPackSummary>>
    fun getSummary(packId: String): Flow<CustomPackSummary?>
    suspend fun getSummaryOnce(packId: String): CustomPackSummary?
    suspend fun insertPack(packRow: QuestionPackEntity, customRow: CustomPackEntity)
    suspend fun upsertPack(customRow: CustomPackEntity)
    suspend fun deletePack(packId: String)
    suspend fun setPackAvailable(packId: String, available: Boolean)

    fun getEntries(packId: String): Flow<List<CustomEntryEntity>>
    suspend fun getEntry(id: String): CustomEntryEntity?
    suspend fun getPlayableEntries(): List<CustomEntryEntity>
    suspend fun nextPosition(packId: String): Int
    suspend fun upsertEntry(entry: CustomEntryEntity)
    suspend fun deleteEntry(id: String)
}
