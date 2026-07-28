package com.restrusher.partypuzl.data.local.appData.appDataSource

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Premium packs unlocked by watching a rewarded ad.
 *
 * Deliberately in-memory: a rewarded unlock lasts for the session only, so it dies with the
 * process. Permanent unlocks come from the purchase and live in Room
 * ([com.restrusher.partypuzl.data.local.entities.QuestionPackEntity.isUnlocked]).
 *
 * Follows the same singleton-object shape as [GameOptionsSource] and [GamePlayersList].
 */
object SessionUnlocksSource {

    private val _unlockedPackIds = MutableStateFlow<Set<String>>(emptySet())
    val unlockedPackIds: StateFlow<Set<String>> = _unlockedPackIds.asStateFlow()

    fun unlock(packId: String) {
        _unlockedPackIds.update { it + packId }
    }

    fun isUnlocked(packId: String): Boolean = packId in _unlockedPackIds.value

    fun clear() {
        _unlockedPackIds.value = emptySet()
    }
}
