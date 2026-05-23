package com.restrusher.partypuzl.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val APP_LANGUAGE_KEY = stringPreferencesKey("app_language")
        private val IS_AD_FREE_KEY = booleanPreferencesKey("is_ad_free")
    }

    override val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        prefs[THEME_MODE_KEY]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.SYSTEM
    }

    override val appLanguage: Flow<AppLanguage> = dataStore.data.map { prefs ->
        prefs[APP_LANGUAGE_KEY]?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() }
            ?: AppLanguage.SYSTEM
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { prefs -> prefs[THEME_MODE_KEY] = themeMode.name }
    }

    override suspend fun setAppLanguage(language: AppLanguage) {
        dataStore.edit { prefs -> prefs[APP_LANGUAGE_KEY] = language.name }
    }

    override val isAdFree: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[IS_AD_FREE_KEY] ?: false
    }

    override suspend fun setAdFree(adFree: Boolean) {
        dataStore.edit { prefs -> prefs[IS_AD_FREE_KEY] = adFree }
    }
}
