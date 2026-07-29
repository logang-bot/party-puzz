package com.restrusher.partypuzl.data.local.proxies

import com.restrusher.partypuzl.data.local.dao.QuestionPackDao
import com.restrusher.partypuzl.data.local.entities.QuestionPackEntity
import com.restrusher.partypuzl.data.proxies.QuestionPackProxy
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class QuestionPackLocalProxy @Inject constructor(
    private val questionPackDao: QuestionPackDao
) : QuestionPackProxy {

    override fun getPacks(): Flow<List<QuestionPackEntity>> = questionPackDao.getAllPacks()

    override suspend fun getPacksOnce(): List<QuestionPackEntity> = questionPackDao.getAllPacksOnce()

    override suspend fun seedMissing(packs: List<QuestionPackEntity>) =
        questionPackDao.insertMissing(packs)

    override suspend fun setEnabled(id: String, enabled: Boolean) =
        questionPackDao.setEnabled(id, enabled)

    override suspend fun setUnlocked(id: String, unlocked: Boolean) =
        questionPackDao.setUnlocked(id, unlocked)

    override suspend fun unlockAllPremium() = questionPackDao.unlockAllPremium()

    override suspend fun deleteRetiredCatalogPacks(keepIds: List<String>) =
        questionPackDao.deleteRetiredCatalogPacks(keepIds)
}
