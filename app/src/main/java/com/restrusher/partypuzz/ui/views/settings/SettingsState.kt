package com.restrusher.partypuzz.ui.views.settings

import com.restrusher.partypuzz.data.preferences.AppLanguage
import com.restrusher.partypuzz.data.preferences.ThemeMode

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
    val isThemeSheetOpen: Boolean = false,
    val isLanguageSheetOpen: Boolean = false
)
