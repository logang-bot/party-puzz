package com.restrusher.partypuzz.data.preferences

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val themeMode: Flow<ThemeMode>
    val appLanguage: Flow<AppLanguage>
    suspend fun setThemeMode(themeMode: ThemeMode)
    suspend fun setAppLanguage(language: AppLanguage)
}
