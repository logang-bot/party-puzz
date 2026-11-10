package com.restrusher.partypuzl.domain.customPacks

import com.restrusher.partypuzl.data.preferences.UserPreferencesRepository
import com.restrusher.partypuzl.data.repositories.interfaces.CustomPackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/** Every pack counts toward the cap, including ones switched off in the manager. */
class ObserveCustomPackQuotaUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val customPackRepository: CustomPackRepository
) {

    operator fun invoke(): Flow<CustomPackQuota> = combine(
        userPreferencesRepository.isAdFree,
        customPackRepository.getSummaries()
    ) { isPremium, packs -> CustomPackQuota(isPremium = isPremium, packCount = packs.size) }
}
