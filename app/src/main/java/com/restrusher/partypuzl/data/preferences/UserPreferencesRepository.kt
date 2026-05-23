package com.restrusher.partypuzl.data.preferences

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val themeMode: Flow<ThemeMode>
    val appLanguage: Flow<AppLanguage>
    val isAdFree: Flow<Boolean>
    suspend fun setThemeMode(themeMode: ThemeMode)
    suspend fun setAppLanguage(language: AppLanguage)
    suspend fun setAdFree(adFree: Boolean)
}
