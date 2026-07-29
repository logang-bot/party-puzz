package com.restrusher.partypuzl.data.repositories

import com.restrusher.partypuzl.data.local.dao.CustomPackSummary
import com.restrusher.partypuzl.data.local.entities.CustomEntryEntity
import com.restrusher.partypuzl.data.local.entities.CustomPackEntity
import com.restrusher.partypuzl.data.local.entities.QuestionPackEntity
import com.restrusher.partypuzl.data.models.CustomEntryDraft
import com.restrusher.partypuzl.data.models.CustomPackDraft
import com.restrusher.partypuzl.data.models.PackTier
import com.restrusher.partypuzl.data.proxies.CustomPackProxy
import com.restrusher.partypuzl.data.repositories.interfaces.CustomPackRepository
import com.restrusher.partypuzl.di.DatabaseProxy
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class CustomPackRepositoryImpl(
    @DatabaseProxy private val customPackLocalProxy: CustomPackProxy
) : CustomPackRepository {

    override fun getSummaries(): Flow<List<CustomPackSummary>> =
        customPackLocalProxy.getSummaries()

    override fun getSummary(packId: String): Flow<CustomPackSummary?> =
        customPackLocalProxy.getSummary(packId)

    override suspend fun getSummaryOnce(packId: String): CustomPackSummary? =
        customPackLocalProxy.getSummaryOnce(packId)

    /**
     * A new pack needs both rows: the `question_packs` row is what the seeder, the setup screen and
     * the content loader see, and it has to exist before the `custom_packs` row's foreign key will
     * accept it. New packs start switched on — the user just wrote one, they mean to play it.
     */
    override suspend fun savePack(draft: CustomPackDraft): String {
        val existingId = draft.id
        if (existingId != null) {
            customPackLocalProxy.upsertPack(draft.toEntity(existingId, createdAt = null))
            return existingId
        }
        val id = "custom_${UUID.randomUUID()}"
        customPackLocalProxy.insertPack(
            packRow = QuestionPackEntity(
                id = id,
                tier = PackTier.CUSTOM,
                category = draft.category,
                isEnabled = true,
                isUnlocked = true
            ),
            customRow = draft.toEntity(id, createdAt = System.currentTimeMillis())
        )
        return id
    }

    override suspend fun deletePack(packId: String) = customPackLocalProxy.deletePack(packId)

    override suspend fun setEnabled(packId: String, enabled: Boolean) =
        customPackLocalProxy.setPackEnabled(packId, enabled)

    override fun getEntries(packId: String): Flow<List<CustomEntryEntity>> =
        customPackLocalProxy.getEntries(packId)

    override suspend fun getEntry(id: String): CustomEntryEntity? =
        customPackLocalProxy.getEntry(id)

    override suspend fun saveEntry(draft: CustomEntryDraft) {
        val existing = draft.id?.let { customPackLocalProxy.getEntry(it) }
        customPackLocalProxy.upsertEntry(
            CustomEntryEntity(
                id = existing?.id ?: "entry_${UUID.randomUUID()}",
                packId = draft.packId,
                type = draft.type,
                text = draft.text,
                durationSeconds = draft.durationSeconds,
                optionA = draft.optionA,
                optionB = draft.optionB,
                correctOption = draft.correctOption?.toString(),
                isEnabled = existing?.isEnabled ?: true,
                // An edit keeps its slot; a new entry goes to the end of the pack.
                position = existing?.position ?: customPackLocalProxy.nextPosition(draft.packId)
            )
        )
    }

    override suspend fun deleteEntry(id: String) = customPackLocalProxy.deleteEntry(id)

    override suspend fun getPlayableEntries(): List<CustomEntryEntity> =
        customPackLocalProxy.getPlayableEntries()

    /** An edit must not reset the creation timestamp, which is the manager's sort key. */
    private suspend fun CustomPackDraft.toEntity(id: String, createdAt: Long?) = CustomPackEntity(
        packId = id,
        name = name,
        description = description,
        category = category,
        spice = spice,
        createdAt = createdAt
            ?: customPackLocalProxy.getSummaryOnce(id)?.createdAt
            ?: System.currentTimeMillis()
    )
}
