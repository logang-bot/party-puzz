package com.restrusher.partypuzz.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
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
}
