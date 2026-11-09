package com.restrusher.partypuzl.ui.common.ads.max

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import com.restrusher.partypuzl.data.preferences.UserPreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Preloads the interstitial shown when a game ends, and reloads it after each showing. */
@Singleton
class MaxInterstitialManager @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    var isAdVisible by mutableStateOf(false)
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isAdFree = false
    private var interstitial: MaxInterstitialAd? = null
    private var retryAttempt = 0

    private val listener = object : MaxAdListener {
        override fun onAdLoaded(ad: MaxAd) { retryAttempt = 0 }
        override fun onAdLoadFailed(adUnitId: String, error: MaxError) = retryLoad()
        override fun onAdDisplayed(ad: MaxAd) { isAdVisible = true }
        override fun onAdHidden(ad: MaxAd) = onDismissed()
        override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) = onDismissed()
        override fun onAdClicked(ad: MaxAd) = Unit
    }

    init {
        scope.launch {
            userPreferencesRepository.isAdFree.collect { isAdFree = it }
        }
    }

    fun loadAd() {
        if (isAdFree) return
        val ad = interstitial ?: createInterstitial()
        if (!ad.isReady) ad.loadAd()
    }

    fun showIfReady(activity: Activity) {
        val ad = interstitial
        if (isAdFree || ad == null || !ad.isReady) return loadAd()
        ad.showAd(activity)
    }

    private fun createInterstitial(): MaxInterstitialAd =
        MaxInterstitialAd(MaxKeys.GAME_END_INTERSTITIAL).also {
            it.setListener(listener)
            interstitial = it
        }

    private fun retryLoad() {
        retryAttempt++
        scope.launch {
            delay(MaxRetryDelay.millisFor(retryAttempt))
            loadAd()
        }
    }

    private fun onDismissed() {
        isAdVisible = false
        loadAd()
    }
}
