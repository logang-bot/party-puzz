package com.restrusher.partypuzl.ui.common

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.restrusher.partypuzl.data.preferences.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Singleton
class AppOpenAdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isAdFree = false
    private var appOpenAd: AppOpenAd? = null
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
        AppOpenAd.load(
            context,
            AdUnitIds.APP_OPEN,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    isLoading = false
                    appOpenAd = ad
                    loadTime = System.currentTimeMillis()
                    pendingActivity?.get()?.let { showAd(it) }
                    pendingActivity = null
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoading = false
                    appOpenAd = null
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
        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() { isAdVisible = true }
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isAdVisible = false
                loadAd()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                appOpenAd = null
                isAdVisible = false
            }
        }
        appOpenAd?.show(activity)
    }

    private fun isAdAvailable() =
        appOpenAd != null && System.currentTimeMillis() - loadTime < 4 * 3_600_000L
}
