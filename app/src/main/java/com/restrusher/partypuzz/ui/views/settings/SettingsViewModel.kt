package com.restrusher.partypuzz.ui.views.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.partypuzz.data.preferences.AppLanguage
import com.restrusher.partypuzz.data.preferences.ThemeMode
import com.restrusher.partypuzz.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                userPreferencesRepository.themeMode,
                userPreferencesRepository.appLanguage
            ) { theme, language -> theme to language }
                .collect { (theme, language) ->
                    _uiState.update { it.copy(themeMode = theme, appLanguage = language) }
                }
        }
    }

    fun openThemeSheet() = _uiState.update { it.copy(isThemeSheetOpen = true) }
    fun closeThemeSheet() = _uiState.update { it.copy(isThemeSheetOpen = false) }
    fun openLanguageSheet() = _uiState.update { it.copy(isLanguageSheetOpen = true) }
    fun closeLanguageSheet() = _uiState.update { it.copy(isLanguageSheetOpen = false) }

    fun selectTheme(themeMode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(themeMode)
            closeThemeSheet()
        }
    }

    fun selectLanguage(language: AppLanguage) {
        viewModelScope.launch {
            userPreferencesRepository.setAppLanguage(language)
            closeLanguageSheet()
        }
    }
}
