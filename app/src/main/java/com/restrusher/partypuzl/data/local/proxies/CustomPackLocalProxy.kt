package com.restrusher.partypuzl.data.local.proxies

import com.restrusher.partypuzl.data.local.dao.CustomEntryDao
import com.restrusher.partypuzl.data.local.dao.CustomPackDao
import com.restrusher.partypuzl.data.local.dao.CustomPackSummary
import com.restrusher.partypuzl.data.local.entities.CustomEntryEntity
import com.restrusher.partypuzl.data.local.entities.CustomPackEntity
import com.restrusher.partypuzl.data.local.entities.QuestionPackEntity
import com.restrusher.partypuzl.data.proxies.CustomPackProxy
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CustomPackLocalProxy @Inject constructor(
    private val customPackDao: CustomPackDao,
    private val customEntryDao: CustomEntryDao
) : CustomPackProxy {

    override fun getSummaries(): Flow<List<CustomPackSummary>> = customPackDao.getSummaries()

    override fun getSummary(packId: String): Flow<CustomPackSummary?> =
        customPackDao.getSummary(packId)

    override suspend fun getSummaryOnce(packId: String): CustomPackSummary? =
        customPackDao.getSummaryOnce(packId)

    override suspend fun insertPack(packRow: QuestionPackEntity, customRow: CustomPackEntity) =
        customPackDao.insertPack(packRow, customRow)

    override suspend fun upsertPack(customRow: CustomPackEntity) = customPackDao.upsert(customRow)

    override suspend fun deletePack(packId: String) = customPackDao.deletePackRow(packId)

    override suspend fun setPackAvailable(packId: String, available: Boolean) =
        customPackDao.setAvailability(packId, available)

    override fun getEntries(packId: String): Flow<List<CustomEntryEntity>> =
        customEntryDao.getForPack(packId)

    override suspend fun getEntry(id: String): CustomEntryEntity? = customEntryDao.getById(id)

    override suspend fun getPlayableEntries(): List<CustomEntryEntity> =
        customEntryDao.getPlayableEntries()

    override suspend fun nextPosition(packId: String): Int = customEntryDao.nextPosition(packId)

    override suspend fun upsertEntry(entry: CustomEntryEntity) = customEntryDao.upsert(entry)

    override suspend fun deleteEntry(id: String) = customEntryDao.delete(id)
}
