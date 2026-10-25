package com.restrusher.partypuzl.ui.common.ads.meta

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener
import com.restrusher.partypuzl.data.preferences.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Preloads the interstitial shown when a game ends, and reloads it after each showing. */
@Singleton
class MetaInterstitialManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    var isAdVisible by mutableStateOf(false)
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isAdFree = false
    private var interstitial: InterstitialAd? = null
    private var isLoaded = false

    private val listener = object : InterstitialAdListener {
        override fun onAdLoaded(ad: Ad) { isLoaded = true }
        override fun onError(ad: Ad, error: AdError) { isLoaded = false }
        override fun onInterstitialDisplayed(ad: Ad) { isAdVisible = true }
        override fun onInterstitialDismissed(ad: Ad) = onDismissed()
        override fun onAdClicked(ad: Ad) = Unit
        override fun onLoggingImpression(ad: Ad) = Unit
    }

    init {
        scope.launch {
            userPreferencesRepository.isAdFree.collect { isAdFree = it }
        }
    }

    fun loadAd() {
        if (isAdFree || isLoaded) return
        interstitial?.destroy()
        val ad = InterstitialAd(context, MetaPlacementIds.GAME_END_INTERSTITIAL)
        interstitial = ad
        ad.loadAd(ad.buildLoadAdConfig().withAdListener(listener).build())
    }

    fun showIfReady() {
        val ad = interstitial ?: return loadAd()
        if (isAdFree || !isLoaded || ad.isAdInvalidated) return loadAd()
        isLoaded = false
        ad.show()
    }

    private fun onDismissed() {
        isAdVisible = false
        loadAd()
    }
}
