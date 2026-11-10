package com.restrusher.partypuzl.ui

import android.os.Bundle
import android.widget.Toast
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
import com.restrusher.partypuzl.data.billing.PremiumBilling
import com.restrusher.partypuzl.data.billing.PurchaseOutcome
import com.restrusher.partypuzl.data.packBank.PackBankRepository
import com.restrusher.partypuzl.data.preferences.AppLanguage
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.data.preferences.UserPreferencesRepository
import com.restrusher.partypuzl.navigation.HomeNavigation
import com.restrusher.partypuzl.ui.common.ResponsibleDrinkingPrompts
import com.restrusher.partypuzl.ui.common.ads.AdsProvider
import com.restrusher.partypuzl.ui.common.ads.LocalAdsProvider
import com.restrusher.partypuzl.ui.common.ads.LocalIsAdFree
import com.restrusher.partypuzl.ui.common.purchase.messageRes
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import androidx.compose.runtime.CompositionLocalProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var adsProvider: AdsProvider

    @Inject
    lateinit var premiumBilling: PremiumBilling

    @Inject
    lateinit var packBankRepository: PackBankRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(ProcessRestart.stateToRestore(savedInstanceState))
        enableEdgeToEdge()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userPreferencesRepository.appLanguage.collect { language ->
                    applyLanguage(language)
                }
            }
        }
        lifecycleScope.launch { premiumBilling.outcomes.collect(::showPurchaseOutcome) }

        setContent {
            val themeMode by userPreferencesRepository.themeMode
                .collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            val isAdFree by userPreferencesRepository.isAdFree
                .collectAsStateWithLifecycle(initialValue = false)
            val drinkingNoticeAccepted by userPreferencesRepository.drinkingNoticeAccepted
                .collectAsStateWithLifecycle(initialValue = null)

            PartyPuzlTheme(themeMode = themeMode) {
                CompositionLocalProvider(
                    LocalIsAdFree provides isAdFree,
                    LocalAdsProvider provides adsProvider
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        HomeNavigation()
                        ResponsibleDrinkingPrompts(
                            noticeAccepted = drinkingNoticeAccepted,
                            onAccept = ::acceptDrinkingNotice,
                            onExit = ::finish
                        )
                        if (adsProvider.isFullScreenAdVisible) {
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
    }

    override fun onStart() {
        super.onStart()
        adsProvider.onActivityStarted(this)
        premiumBilling.refreshEntitlement()
        lifecycleScope.launch { packBankRepository.refresh() }
    }

    override fun onStop() {
        super.onStop()
        adsProvider.onActivityStopped()
    }

    private fun showPurchaseOutcome(outcome: PurchaseOutcome) {
        val messageRes = outcome.messageRes ?: return
        Toast.makeText(this, messageRes, Toast.LENGTH_LONG).show()
    }

    private fun acceptDrinkingNotice() {
        lifecycleScope.launch { userPreferencesRepository.acceptDrinkingNotice() }
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
