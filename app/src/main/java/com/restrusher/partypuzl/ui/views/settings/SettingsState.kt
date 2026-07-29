package com.restrusher.partypuzl.ui.views.settings

import com.restrusher.partypuzl.data.preferences.AppLanguage
import com.restrusher.partypuzl.data.preferences.ThemeMode

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
    val isThemeSheetOpen: Boolean = false,
    val isLanguageSheetOpen: Boolean = false,
    val isAdFree: Boolean = false,
    /** How many of the user's own packs are switched on — the "Custom packs" row's subtitle. */
    val activeCustomPacks: Int = 0
)
