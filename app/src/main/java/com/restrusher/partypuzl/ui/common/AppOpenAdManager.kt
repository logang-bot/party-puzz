package com.restrusher.partypuzl.ui.common

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.restrusher.partypuzl.data.preferences.UserPreferencesRepository
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Unity Ads has no dedicated "App Open" ad format, so this manager shows an
 * interstitial ([AdPlacementIds.APP_OPEN_INTERSTITIAL]) on app launch / foreground
 * return in place of AdMob's App Open Ad, keeping the same load/show lifecycle.
 */
@Singleton
class AppOpenAdManager @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isAdFree = false
    private var isAdLoaded = false
    private var isLoading = false
    var isAdVisible by mutableStateOf(false)
        private set
    private var loadTime: Long = 0
    private var pendingActivity: WeakReference<Activity>? = null

    init {
        scope.launch {
            userPreferencesRepository.isAdFree.collect { isAdFree = it }
        }
    }

    fun loadAd() {
        if (isAdFree || isAdAvailable() || isLoading) return
        isLoading = true
        UnityAds.load(
            AdPlacementIds.APP_OPEN_INTERSTITIAL,
            object : IUnityAdsLoadListener {
                override fun onUnityAdsAdLoaded(placementId: String) {
                    isLoading = false
                    isAdLoaded = true
                    loadTime = System.currentTimeMillis()
                    pendingActivity?.get()?.let { showAd(it) }
                    pendingActivity = null
                }
                override fun onUnityAdsFailedToLoad(
                    placementId: String,
                    error: UnityAds.UnityAdsLoadError,
                    message: String
                ) {
                    isLoading = false
                    isAdLoaded = false
                    pendingActivity = null
                }
            }
        )
    }

    fun showWhenReady(activity: Activity) {
        if (isAdFree) return
        if (isAdAvailable()) {
            showAd(activity)
        } else {
            pendingActivity = WeakReference(activity)
            loadAd()
        }
    }

    fun clearPendingActivity() {
        pendingActivity = null
    }

    private fun showAd(activity: Activity) {
        if (isAdVisible || activity.isFinishing || activity.isDestroyed) return
        isAdVisible = true
        UnityAds.show(
            activity,
            AdPlacementIds.APP_OPEN_INTERSTITIAL,
            object : IUnityAdsShowListener {
                override fun onUnityAdsShowFailure(
                    placementId: String,
                    error: UnityAds.UnityAdsShowError,
                    message: String
                ) {
                    isAdLoaded = false
                    isAdVisible = false
                }
                override fun onUnityAdsShowStart(placementId: String) {}
                override fun onUnityAdsShowClick(placementId: String) {}
                override fun onUnityAdsShowComplete(
                    placementId: String,
                    state: UnityAds.UnityAdsShowCompletionState
                ) {
                    isAdLoaded = false
                    isAdVisible = false
                    loadAd()
                }
            }
        )
    }

    private fun isAdAvailable() =
        isAdLoaded && System.currentTimeMillis() - loadTime < 4 * 3_600_000L
}
