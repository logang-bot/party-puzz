package com.restrusher.partypuzl.data.preferences

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val themeMode: Flow<ThemeMode>
    val appLanguage: Flow<AppLanguage>
    val isAdFree: Flow<Boolean>

    /**
     * Version of the question→pack mapping the `questions` table was built from. Compared with
     * `QuestionCatalog.MAPPING_VERSION` to decide whether the rows need rebuilding.
     */
    suspend fun getQuestionMappingVersion(): Int
    suspend fun setQuestionMappingVersion(version: Int)
    suspend fun setThemeMode(themeMode: ThemeMode)
    suspend fun setAppLanguage(language: AppLanguage)
    suspend fun setAdFree(adFree: Boolean)
}
