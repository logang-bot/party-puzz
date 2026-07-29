package com.restrusher.partypuzl.data.repositories

import com.restrusher.partypuzl.data.local.appData.appDataSource.QuestionPackCatalog
import com.restrusher.partypuzl.data.local.entities.QuestionPackEntity
import com.restrusher.partypuzl.data.proxies.QuestionPackProxy
import com.restrusher.partypuzl.data.repositories.interfaces.QuestionPackRepository
import com.restrusher.partypuzl.di.DatabaseProxy
import kotlinx.coroutines.flow.Flow

class QuestionPackRepositoryImpl(
    @DatabaseProxy private val questionPackLocalProxy: QuestionPackProxy
) : QuestionPackRepository {

    override fun getPacks(): Flow<List<QuestionPackEntity>> = questionPackLocalProxy.getPacks()

    override suspend fun getPacksOnce(): List<QuestionPackEntity> =
        questionPackLocalProxy.getPacksOnce()

    override suspend fun seedFromCatalog() {
        questionPackLocalProxy.seedMissing(QuestionPackCatalog.defaultEntities())
    }

    override suspend fun setEnabled(id: String, enabled: Boolean) =
        questionPackLocalProxy.setEnabled(id, enabled)

    override suspend fun setUnlocked(id: String, unlocked: Boolean) =
        questionPackLocalProxy.setUnlocked(id, unlocked)

    override suspend fun unlockAllPremium() = questionPackLocalProxy.unlockAllPremium()

    override suspend fun deleteRetiredCatalogPacks(keepIds: List<String>) =
        questionPackLocalProxy.deleteRetiredCatalogPacks(keepIds)
}
