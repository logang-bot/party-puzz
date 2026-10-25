package com.restrusher.partypuzl.ui.common.ads.admob

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

/** Loads an App Open ad and shows it on every launch / return to the foreground. */
@Singleton
class AdMobAppOpenManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    var isAdVisible by mutableStateOf(false)
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isAdFree = false
    private var appOpenAd: AppOpenAd? = null
    private var isLoading = false
    private var loadTime: Long = 0
    private var pendingActivity: WeakReference<Activity>? = null

    private val loadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
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

    private val showCallback = object : FullScreenContentCallback() {
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

    init {
        scope.launch {
            userPreferencesRepository.isAdFree.collect { isAdFree = it }
        }
    }

    fun loadAd() {
        if (isAdFree || isAdAvailable() || isLoading) return
        isLoading = true
        AppOpenAd.load(context, AdMobUnitIds.appOpen, AdRequest.Builder().build(), loadCallback)
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
        appOpenAd?.fullScreenContentCallback = showCallback
        appOpenAd?.show(activity)
    }

    private fun isAdAvailable() =
        appOpenAd != null && System.currentTimeMillis() - loadTime < MAX_AD_AGE_MS

    private companion object {
        const val MAX_AD_AGE_MS = 4 * 3_600_000L
    }
}
