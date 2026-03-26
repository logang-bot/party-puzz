package com.restrusher.partypuzz.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import androidx.compose.runtime.getValue
import com.restrusher.partypuzz.data.preferences.AppLanguage
import com.restrusher.partypuzz.data.preferences.ThemeMode
import com.restrusher.partypuzz.data.preferences.UserPreferencesRepository
import com.restrusher.partypuzz.navigation.HomeNavigation
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userPreferencesRepository.appLanguage.collect { language ->
                    applyLanguage(language)
                }
            }
        }

        setContent {
            val themeMode by userPreferencesRepository.themeMode
                .collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)

            PartyPuzzTheme(themeMode = themeMode) {
                HomeNavigation()
            }
        }
    }

    private fun applyLanguage(language: AppLanguage) {
        val newLocales = when (language) {
            AppLanguage.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
            else -> LocaleListCompat.forLanguageTags(language.tag)
        }
        if (AppCompatDelegate.getApplicationLocales().toLanguageTags() != newLocales.toLanguageTags()) {
            AppCompatDelegate.setApplicationLocales(newLocales)
        }
    }
}
