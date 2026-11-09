package com.restrusher.partypuzl.ui.common.ads.levelplay

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.restrusher.partypuzl.data.preferences.UserPreferencesRepository
import com.unity3d.mediation.LevelPlayAdError
import com.unity3d.mediation.LevelPlayAdInfo
import com.unity3d.mediation.interstitial.LevelPlayInterstitialAd
import com.unity3d.mediation.interstitial.LevelPlayInterstitialAdListener
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Preloads the interstitial shown when a game ends, and reloads it after each showing. */
@Singleton
class LevelPlayInterstitialManager @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    var isAdVisible by mutableStateOf(false)
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isAdFree = false
    private var interstitial: LevelPlayInterstitialAd? = null
    private var retryAttempt = 0

    private val listener = object : LevelPlayInterstitialAdListener {
        override fun onAdLoaded(adInfo: LevelPlayAdInfo) { retryAttempt = 0 }
        override fun onAdLoadFailed(error: LevelPlayAdError) = retryLoad()
        override fun onAdDisplayed(adInfo: LevelPlayAdInfo) { isAdVisible = true }
        override fun onAdClosed(adInfo: LevelPlayAdInfo) = onDismissed()
        override fun onAdDisplayFailed(error: LevelPlayAdError, adInfo: LevelPlayAdInfo) = onDismissed()
    }

    init {
        scope.launch {
            userPreferencesRepository.isAdFree.collect { isAdFree = it }
        }
    }

    fun loadAd() {
        if (isAdFree) return
        val ad = interstitial ?: createInterstitial()
        if (!ad.isAdReady()) ad.loadAd()
    }

    fun showIfReady(activity: Activity) {
        val ad = interstitial
        if (isAdFree || ad == null || !ad.isAdReady()) return loadAd()
        ad.showAd(activity)
    }

    private fun createInterstitial(): LevelPlayInterstitialAd =
        LevelPlayInterstitialAd(LevelPlayKeys.GAME_END_INTERSTITIAL).also {
            it.setListener(listener)
            interstitial = it
        }

    private fun retryLoad() {
        retryAttempt++
        scope.launch {
            delay(LevelPlayRetryDelay.millisFor(retryAttempt))
            loadAd()
        }
    }

    private fun onDismissed() {
        isAdVisible = false
        loadAd()
    }
}
