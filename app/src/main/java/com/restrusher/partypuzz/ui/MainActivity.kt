package com.restrusher.partypuzz.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.restrusher.partypuzz.data.preferences.AppLanguage
import com.restrusher.partypuzz.data.preferences.ThemeMode
import com.restrusher.partypuzz.data.preferences.UserPreferencesRepository
import com.restrusher.partypuzz.navigation.HomeNavigation
import com.restrusher.partypuzz.ui.common.AppOpenAdManager
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var appOpenAdManager: AppOpenAdManager

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
                Box(modifier = Modifier.fillMaxSize()) {
                    HomeNavigation()
                    if (appOpenAdManager.isAdVisible) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        appOpenAdManager.showWhenReady(this)
    }

    override fun onStop() {
        super.onStop()
        appOpenAdManager.clearPendingActivity()
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
